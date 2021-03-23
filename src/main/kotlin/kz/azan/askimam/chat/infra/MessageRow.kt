package kz.azan.askimam.chat.infra

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.Clock
import java.time.LocalDateTime

data class MessageRow(
    val id: Long?,
    val type: Message.Type,
    val authorId: Long,
    val text: String,
    val audio: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(message: Message) = MessageRow(
            message.id?.value,
            message.type,
            message.authorId.value,
            message.text().value,
            message.audio?.value,
            message.createdAt,
            message.updatedAt(),
        )
    }

    fun toDomain(clock: Clock) = Message.restore(
        clock,
        Message.Id(id!!),
        type,
        User.Id(authorId),
        NonBlankString.of(text),
        audio?.run { NonBlankString.of(audio) },
        createdAt,
        updatedAt,
    )
}
