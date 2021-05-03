package kz.azan.askimam.security.web.dto

import javax.validation.constraints.NotBlank

data class LogoutDto(
    @get:NotBlank
    val fcmToken: String
)
