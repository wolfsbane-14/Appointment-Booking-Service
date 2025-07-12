package com.appointmentbooking.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BookingConflictException::class)
    fun handleConflict(e: BookingConflictException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)

    @ExceptionHandler(BookingNotFoundException::class)
    fun handleNotFound(e: BookingNotFoundException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error")
}
