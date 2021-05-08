package kz.azan.askimam.chat.web.dto

import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.Chat

data class ChatDto(
    val id: Long,
    val type: Chat.Type,
    val askedBy: Long,
    val subject: String,
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
                isFavorite = chat.isFavorite,
                isViewedByImam = chat.isViewedByImam,
                isViewedByInquirer = chat.isViewedByInquirer,
                messages = chat.messages?.map { MessageDto.from(it) }?.toJavaList(),
            )
    }
}
