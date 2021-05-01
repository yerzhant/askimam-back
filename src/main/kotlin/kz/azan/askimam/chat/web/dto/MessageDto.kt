package kz.azan.askimam.chat.web.dto

import kz.azan.askimam.chat.app.projection.MessageProjection
import kz.azan.askimam.chat.domain.model.Message
import java.time.LocalDateTime

data class MessageDto(
    val id: Long,
    val type: Message.Type,
    val text: String,
    val audio: String?,
    val author: String?,
    val createdAt: LocalDateTime, // TODO: these two time are to be converted to zoned ones
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(message: MessageProjection) = MessageDto(
            id = message.id.value,
            type = message.type,
            text = message.text.value,
            audio = message.audio?.value,
            author = message.author?.name?.value,
            createdAt = message.createdAt,
            updatedAt = message.updatedAt,
        )
    }
}
