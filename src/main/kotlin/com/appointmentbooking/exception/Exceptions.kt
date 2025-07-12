package com.appointmentbooking.exception

class BookingConflictException(message: String) : RuntimeException(message)

class BookingNotFoundException(message: String) : RuntimeException(message)
