package kz.azan.askimam.chat.app.projection

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Subject

data class ChatProjection(
    val id: Chat.Id,
    val subject: Subject,
    val isFavorite: Boolean = false,
    var messages: List<MessageProjection>? = null,
) {
    companion object {
        fun from(chat: Chat) = ChatProjection(
            chat.id!!,
            chat.subjectText(),
            messages = chat.messages().map { MessageProjection.from(it) },
        )
    }
}
