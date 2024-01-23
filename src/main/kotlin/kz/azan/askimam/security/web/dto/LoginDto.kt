package kz.azan.askimam.security.web.dto

import jakarta.validation.constraints.NotBlank

data class LoginDto(
    @get:NotBlank
    val login: String,

    @get:NotBlank
    val password: String,

    @get:NotBlank
    val fcmToken: String,
)
