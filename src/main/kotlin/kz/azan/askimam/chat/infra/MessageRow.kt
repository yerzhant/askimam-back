package kz.azan.askimam.chat.infra

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import org.springframework.data.relational.core.mapping.Table
import java.time.Clock
import java.time.LocalDateTime

@Table("MESSAGES")
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
            id = message.id?.value,
            type = message.type,
            authorId = message.authorId.value,

            text = message.text().value,
            audio = message.audio?.value,

            createdAt = message.createdAt,
            updatedAt = message.updatedAt(),
        )
    }

    fun toDomain(clock: Clock) = Message.restore(
        id = Message.Id(id!!),
        type = type,
        authorId = User.Id(authorId),

        text = NonBlankString.of(text),
        audio = audio?.run { NonBlankString.of(audio) },

        createdAt = createdAt,
        updatedAt = updatedAt,

        clock = clock,
    )
}
