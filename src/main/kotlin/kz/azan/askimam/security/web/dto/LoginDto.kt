package kz.azan.askimam.security.web.dto

import javax.validation.constraints.NotBlank

data class LoginDto(
    @get:NotBlank
    val login: String,

    @get:NotBlank
    val password: String,

    @get:NotBlank
    val fcmToken: String,
)
