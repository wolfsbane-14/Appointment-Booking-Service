package com.appointmentbooking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class AppointmentBookingServiceApplication

fun main(args: Array<String>) {
    runApplication<AppointmentBookingServiceApplication>(*args)
}