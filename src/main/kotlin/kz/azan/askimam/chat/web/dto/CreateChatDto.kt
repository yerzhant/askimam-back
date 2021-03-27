package kz.azan.askimam.chat.web.dto

import kz.azan.askimam.chat.domain.model.Chat

data class CreateChatDto(
    val type: Chat.Type,
    val subject: String?,
    val text: String,
    val fcmToken: String,
)
