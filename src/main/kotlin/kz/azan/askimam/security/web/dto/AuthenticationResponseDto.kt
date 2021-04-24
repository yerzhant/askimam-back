package kz.azan.askimam.security.web.dto

import kz.azan.askimam.user.domain.model.User

data class AuthenticationResponseDto(
    val jwt: String,
    val userId: Long,
    val userType: User.Type,
)
