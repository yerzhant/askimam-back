package kz.azan.askimam.security.web.dto

import kz.azan.askimam.user.domain.model.User

data class LoginResponseDto(
    val jwt: String,
    val userId: Long,
    val userType: User.Type,
    val fcmToken: String,
)
