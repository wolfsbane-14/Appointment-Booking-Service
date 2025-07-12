package com.appointmentbooking.controller

import com.appointmentbooking.exception.BookingConflictException
import com.appointmentbooking.exception.BookingNotFoundException

import com.appointmentbooking.dto.*
import com.appointmentbooking.service.BookingService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = ["*"])
class BookingController(
    private val bookingService: BookingService
) {
    
    /**
     * POST /bookings - Create a booking; reject overlaps → 201 or 409
     */
    @PostMapping
    fun createBooking(@Valid @RequestBody request: CreateBookingRequest): ResponseEntity<BookingResponse> {
        return try {
            val booking = bookingService.createBooking(request)
            ResponseEntity.status(HttpStatus.CREATED).body(booking)
        } catch (ex: BookingConflictException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
    
    /**
     * GET /bookings - List bookings (filter by clientId, professionalId, date; optional pagination) → 200
     */
    @GetMapping
    fun listBookings(
        @RequestParam(required = false) clientId: String?,
        @RequestParam(required = false) professionalId: String?,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<BookingListResponse> {
        val bookings = bookingService.listBookings(clientId, professionalId, date, page, size)
        return ResponseEntity.ok(bookings)
    }
    
    /**
     * DELETE /bookings/{id} - Remove a booking → 204 or 404
     */
    @DeleteMapping("/{id}")
    fun deleteBooking(@PathVariable id: Long): ResponseEntity<Unit> {
        return try {
            bookingService.deleteBooking(id)
            ResponseEntity.noContent().build()
        } catch (ex: BookingNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * GET /bookings/availability - Show free slots for a professional on a date → 200
     */
    @GetMapping("/availability")
    fun getAvailability(
        @RequestParam professionalId: String,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate
    ): ResponseEntity<AvailabilityResponse> {
        val availability = bookingService.getAvailability(professionalId, date)
        return ResponseEntity.ok(availability)
    }
}