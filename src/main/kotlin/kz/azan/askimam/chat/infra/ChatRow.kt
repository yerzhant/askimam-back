package kz.azan.askimam.chat.infra

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.user.domain.model.User
import java.time.Clock
import java.time.LocalDateTime

data class ChatRow(
    val id: Long?,
    val type: Chat.Type,
    val askedBy: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val subject: String? = null,
    val messages: Set<MessageRow>,
    val isVisibleToPublic: Boolean,
    val isViewedByImam: Boolean,
    val isViewedByInquirer: Boolean,
) {
    companion object {
        fun from(chat: Chat) = ChatRow(
            chat.id?.value,
            chat.type,
            chat.askedBy.value,
            chat.createdAt,
            chat.updatedAt(), chat.subject()?.value?.value,
            chat.messages().map { MessageRow.from(it) }.toSet(),
            chat.isVisibleToPublic(),
            chat.isViewedByImam(),
            chat.isViewedByInquirer(),
        )
    }

    fun toDomain(clock: Clock, eventPublisher: EventPublisher, getCurrentUser: GetCurrentUser) = Chat.restore(
        clock,
        eventPublisher,
        getCurrentUser,
        Chat.Id(id!!),
        type,
        User.Id(askedBy),
        createdAt,
        updatedAt,
        subject?.run { Subject.from(subject) },
        messages.map { it.toDomain(clock) },
        isVisibleToPublic,
        isViewedByImam,
        isViewedByInquirer,
    )
}
