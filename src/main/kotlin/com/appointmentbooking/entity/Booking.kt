package com.appointmentbooking.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "bookings")
data class Booking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    @NotBlank(message = "Client ID cannot be blank")
    val clientId: String,
    
    @Column(nullable = false)
    @NotBlank(message = "Professional ID cannot be blank")
    val professionalId: String,
    
    @Column(nullable = false)
    @NotNull(message = "Start time cannot be null")
    val startTime: LocalDateTime,
    
    @Column(nullable = false)
    @NotNull(message = "End time cannot be null")
    val endTime: LocalDateTime,
    
    @Column(nullable = false)
    @NotBlank(message = "Service type cannot be blank")
    val serviceType: String,
    
    @Column(nullable = true)
    val notes: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)