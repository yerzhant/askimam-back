package kz.azan.askimam.chat.web.dto

data class AddAudioMessageDto(
    val chatId: Long,
    val audio: String,
    val fcmToken: String,
)
