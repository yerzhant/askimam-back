package kz.azan.askimam.chat.app.projection

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Subject

data class ChatProjection(
    val id: Chat.Id,
    val subject: Subject,
    val isFavorite: Boolean = false,
)
