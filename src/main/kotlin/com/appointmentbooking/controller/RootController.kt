package com.appointmentbooking.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController {

    @GetMapping("/")
    fun home(): String = "✅ Appointment Booking Service is running"
}
