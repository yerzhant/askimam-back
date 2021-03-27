package kz.azan.askimam.chat.app.projection

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.user.domain.model.UserRepository

data class ChatProjection(
    val id: Chat.Id,
    val subject: Subject,
    val isFavorite: Boolean = false,
    var messages: List<MessageProjection>? = null,
) {
    companion object {
        fun from(chat: Chat, userRepository: UserRepository) = ChatProjection(
            chat.id!!,
            chat.subjectText(),
            messages = chat.messages().map { MessageProjection.from(it, userRepository) },
        )
    }
}