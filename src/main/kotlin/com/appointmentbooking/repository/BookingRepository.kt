package com.appointmentbooking.repository

import com.appointmentbooking.entity.Booking
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BookingRepository : JpaRepository<Booking, Long> {
    
    // Pagination support for all filter combinations
    fun findByClientId(clientId: String, pageable: Pageable): Page<Booking>
    fun findByProfessionalId(professionalId: String, pageable: Pageable): Page<Booking>
    fun findByStartTimeBetween(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Page<Booking>
    
    // Combined filters with pagination
    fun findByClientIdAndProfessionalId(clientId: String, professionalId: String, pageable: Pageable): Page<Booking>
    fun findByClientIdAndStartTimeBetween(clientId: String, start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Page<Booking>
    fun findByProfessionalIdAndStartTimeBetween(professionalId: String, start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Page<Booking>
    fun findByClientIdAndProfessionalIdAndStartTimeBetween(
        clientId: String, 
        professionalId: String, 
        start: LocalDateTime, 
        end: LocalDateTime, 
        pageable: Pageable
    ): Page<Booking>
    
    // Non-paginated versions for availability checking
    fun findByProfessionalIdAndStartTimeBetween(
        professionalId: String, 
        start: LocalDateTime, 
        end: LocalDateTime
    ): List<Booking>
    
    // Conflict detection query - critical for concurrency safety
    @Query("""
        SELECT b FROM Booking b 
        WHERE b.professionalId = :professionalId 
        AND (
            (b.startTime < :endTime AND b.endTime > :startTime)
        )
    """)
    fun findConflictingBookings(
        @Param("professionalId") professionalId: String,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime
    ): List<Booking>
}