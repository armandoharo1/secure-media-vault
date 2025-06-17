package com.securemediavault.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "The user anme can't be blanck")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 character")
    val username: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 3, max = 100, message = "Password must be at least 3 characters long")
    val password: String
)