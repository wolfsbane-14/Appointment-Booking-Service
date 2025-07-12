package com.appointmentbooking.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * Request DTO for creating bookings
 */
data class CreateBookingRequest(
    @field:NotBlank(message = "Client ID is required")
    val clientId: String,
    
    @field:NotBlank(message = "Professional ID is required")
    val professionalId: String,
    
    @field:NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startTime: LocalDateTime,
    
    @field:NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endTime: LocalDateTime,
    
    @field:NotBlank(message = "Service type is required")
    val serviceType: String,
    
    val notes: String? = null
)

/**
 * Response DTO for booking details
 */
data class BookingResponse(
    val id: Long,
    val clientId: String,
    val professionalId: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startTime: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endTime: LocalDateTime,
    val serviceType: String,
    val notes: String?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime
)

/**
 * Paginated response for listing bookings
 */
data class BookingListResponse(
    val bookings: List<BookingResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

/**
 * Available time slot
 */
data class AvailabilitySlot(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startTime: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endTime: LocalDateTime
)

/**
 * Availability response for a professional on a specific date
 */
data class AvailabilityResponse(
    val professionalId: String,
    val date: String,
    val availableSlots: List<AvailabilitySlot>
)