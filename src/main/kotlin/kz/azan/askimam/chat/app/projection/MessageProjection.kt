package kz.azan.askimam.chat.app.projection

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.UserRepository
import java.time.LocalDateTime

data class MessageProjection(
    val id: Message.Id,
    val type: Message.Type,
    val text: NonBlankString,
    val author: User?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(message: Message, userRepository: UserRepository) = MessageProjection(
            message.id!!,
            message.type,
            message.text(),
            if (message.authorType == User.Type.Imam) userRepository.findById(message.authorId) else null,
            message.createdAt,
            message.updatedAt(),
        )
    }
}
