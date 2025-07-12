package com.appointmentbooking.service

import com.appointmentbooking.dto.*
import com.appointmentbooking.entity.Booking
import com.appointmentbooking.repository.BookingRepository
import com.appointmentbooking.exception.BookingConflictException
import com.appointmentbooking.exception.BookingNotFoundException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.locks.ReentrantLock

@Service
@Transactional
class BookingService(
    private val bookingRepository: BookingRepository
) {
    
    // Concurrency safety: Lock per professional to prevent race conditions
    private val professionalLocks = mutableMapOf<String, ReentrantLock>()
    
    /**
     * Create booking with concurrency safety and overlap detection
     */
    fun createBooking(request: CreateBookingRequest): BookingResponse {
        // Get or create lock for this professional
        val lock = professionalLocks.getOrPut(request.professionalId) { ReentrantLock() }
        
        lock.lock()
        try {
            // Check for overlapping bookings within the locked section
            val conflicts = bookingRepository.findConflictingBookings(
                request.professionalId,
                request.startTime,
                request.endTime
            )
            
            if (conflicts.isNotEmpty()) {
                throw BookingConflictException("Time slot conflicts with existing booking")
            }
            
            // Create and save the booking
            val booking = Booking(
                clientId = request.clientId,
                professionalId = request.professionalId,
                startTime = request.startTime,
                endTime = request.endTime,
                serviceType = request.serviceType,
                notes = request.notes
            )
            
            val savedBooking = bookingRepository.save(booking)
            
            // Evict cache for this professional's availability
            evictAvailabilityCache(request.professionalId, request.startTime.toLocalDate())
            
            return savedBooking.toResponse()
            
        } finally {
            lock.unlock()
        }
    }
    
    /**
     * List bookings with filtering and pagination
     */
    @Transactional(readOnly = true)
    fun listBookings(
        clientId: String?,
        professionalId: String?,
        date: LocalDate?,
        page: Int,
        size: Int
    ): BookingListResponse {
        
        val pageable: Pageable = PageRequest.of(page, size, Sort.by("startTime").ascending())
        
        val bookingsPage = when {
            // Filter by client, professional, and date
            clientId != null && professionalId != null && date != null -> {
                val startOfDay = date.atStartOfDay()
                val endOfDay = date.atTime(LocalTime.MAX)
                bookingRepository.findByClientIdAndProfessionalIdAndStartTimeBetween(
                    clientId, professionalId, startOfDay, endOfDay, pageable
                )
            }
            // Filter by client and professional
            clientId != null && professionalId != null -> {
                bookingRepository.findByClientIdAndProfessionalId(clientId, professionalId, pageable)
            }
            // Filter by client and date
            clientId != null && date != null -> {
                val startOfDay = date.atStartOfDay()
                val endOfDay = date.atTime(LocalTime.MAX)
                bookingRepository.findByClientIdAndStartTimeBetween(clientId, startOfDay, endOfDay, pageable)
            }
            // Filter by professional and date
            professionalId != null && date != null -> {
                val startOfDay = date.atStartOfDay()
                val endOfDay = date.atTime(LocalTime.MAX)
                bookingRepository.findByProfessionalIdAndStartTimeBetween(professionalId, startOfDay, endOfDay, pageable)
            }
            // Filter by client only
            clientId != null -> bookingRepository.findByClientId(clientId, pageable)
            // Filter by professional only
            professionalId != null -> bookingRepository.findByProfessionalId(professionalId, pageable)
            // Filter by date only
            date != null -> {
                val startOfDay = date.atStartOfDay()
                val endOfDay = date.atTime(LocalTime.MAX)
                bookingRepository.findByStartTimeBetween(startOfDay, endOfDay, pageable)
            }
            // No filters - return all
            else -> bookingRepository.findAll(pageable)
        }
        
        return BookingListResponse(
            bookings = bookingsPage.content.map { it.toResponse() },
            totalElements = bookingsPage.totalElements,
            totalPages = bookingsPage.totalPages,
            currentPage = page,
            pageSize = size
        )
    }
    
    /**
     * Delete booking
     */
    fun deleteBooking(id: Long) {
        val booking = bookingRepository.findById(id)
            .orElseThrow { BookingNotFoundException("Booking not found with id: $id") }
        
        bookingRepository.deleteById(id)
        
        // Evict cache for this professional's availability
        evictAvailabilityCache(booking.professionalId, booking.startTime.toLocalDate())
    }
    
    /**
     * Get availability with caching
     */
    @Cacheable(value = ["availability"], key = "#professionalId + '_' + #date")
    @Transactional(readOnly = true)
    fun getAvailability(professionalId: String, date: LocalDate): AvailabilityResponse {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        
        // Get all bookings for this professional on this date
        val bookings = bookingRepository.findByProfessionalIdAndStartTimeBetween(
            professionalId, startOfDay, endOfDay
        )
        
        // Generate available slots (9 AM to 5 PM, 1-hour slots)
        val availableSlots = generateAvailableSlots(date, bookings)
        
        return AvailabilityResponse(
            professionalId = professionalId,
            date = date.toString(),
            availableSlots = availableSlots
        )
    }
    
    /**
     * Generate available time slots
     */
    private fun generateAvailableSlots(date: LocalDate, bookings: List<Booking>): List<AvailabilitySlot> {
        val slots = mutableListOf<AvailabilitySlot>()
        
        // Generate hourly slots from 9 AM to 5 PM
        for (hour in 9..16) {
            val slotStart = date.atTime(hour, 0)
            val slotEnd = slotStart.plusHours(1)
            
            // Check if this slot conflicts with any existing booking
            val hasConflict = bookings.any { booking ->
                booking.startTime.isBefore(slotEnd) && booking.endTime.isAfter(slotStart)
            }
            
            if (!hasConflict) {
                slots.add(AvailabilitySlot(slotStart, slotEnd))
            }
        }
        
        return slots
    }
    
    /**
     * Evict availability cache for a professional
     */
    @CacheEvict(value = ["availability"], key = "#professionalId + '_' + #date")
    private fun evictAvailabilityCache(professionalId: String, date: LocalDate) {
        // Cache eviction handled by annotation
    }
    
    /**
     * Extension function to convert Entity to Response DTO
     */
    private fun Booking.toResponse(): BookingResponse {
        return BookingResponse(
            id = this.id,
            clientId = this.clientId,
            professionalId = this.professionalId,
            startTime = this.startTime,
            endTime = this.endTime,
            serviceType = this.serviceType,
            notes = this.notes,
            createdAt = this.createdAt
        )
    }
}

// Custom exceptions - moved to separate file