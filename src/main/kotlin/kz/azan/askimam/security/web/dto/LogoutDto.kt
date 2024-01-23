package kz.azan.askimam.security.web.dto

import jakarta.validation.constraints.NotBlank

data class LogoutDto(
    @get:NotBlank
    val fcmToken: String
)
