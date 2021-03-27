package kz.azan.askimam.chat.web.dto

data class UpdateTextMessageDto(
    val text: String,
    val fcmToken: String,
)
