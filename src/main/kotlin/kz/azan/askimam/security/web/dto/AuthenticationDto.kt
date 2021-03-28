package kz.azan.askimam.security.web.dto

import javax.validation.constraints.NotBlank

data class AuthenticationDto(
    @get:NotBlank
    val login: String,

    @get:NotBlank
    val password: String,
)
