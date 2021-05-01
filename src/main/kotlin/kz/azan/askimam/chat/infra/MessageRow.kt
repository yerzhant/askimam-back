package kz.azan.askimam.chat.infra

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import org.springframework.data.relational.core.mapping.Table
import java.time.Clock
import java.time.LocalDateTime

@Table("messages")
data class MessageRow(
    val id: Long?,
    val type: Message.Type,
    val text: String,
    val audio: String?,
    val duration: String?,

    val authorId: Long,
    val authorType: User.Type,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(message: Message) = MessageRow(
            id = message.id?.value,
            type = message.type,
            text = message.text().value,
            audio = message.audio?.value,
            duration = message.duration?.value,

            authorId = message.authorId.value,
            authorType = message.authorType,

            createdAt = message.createdAt,
            updatedAt = message.updatedAt(),
        )
    }

    fun toDomain(clock: Clock) = Message.restore(
        id = Message.Id(id!!),
        type = type,
        text = NonBlankString.of(text),
        audio = audio?.run { NonBlankString.of(audio) },
        duration = duration?.run { NonBlankString.of(duration) },

        authorId = User.Id(authorId),
        authorType = authorType,

        createdAt = createdAt,
        updatedAt = updatedAt,

        clock = clock,
    )
}
