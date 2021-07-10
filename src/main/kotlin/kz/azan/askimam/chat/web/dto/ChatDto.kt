package kz.azan.askimam.chat.web.dto

import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.Chat
import java.time.LocalDateTime

data class ChatDto(
    val id: Long,
    val type: Chat.Type,
    val askedBy: Long,
    val subject: String,
    val updatedAt: LocalDateTime,
    val isFavorite: Boolean,
    val isViewedByImam: Boolean,
    val isViewedByInquirer: Boolean,
    val messages: List<MessageDto>?,
) {
    companion object {
        fun from(chat: ChatProjection) =
            ChatDto(
                id = chat.id.value,
                type = chat.type,
                askedBy = chat.askedBy.value,
                subject = chat.subject.string(),
                updatedAt = chat.updatedAt,
                isFavorite = chat.isFavorite,
                isViewedByImam = chat.isViewedByImam,
                isViewedByInquirer = chat.isViewedByInquirer,
                messages = chat.messages?.map { MessageDto.from(it) }?.toJavaList(),
            )
    }
}
