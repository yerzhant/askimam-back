package kz.azan.askimam.chat.web.dto

data class AddTextMessageDto(
    val chatId: Long,
    val text: String,
    val fcmToken: String,
)
