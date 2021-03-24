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
    val subject: String?,
    val askedBy: Long,
    val answeredBy: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isVisibleToPublic: Boolean,
    val isViewedByImam: Boolean,
    val isViewedByInquirer: Boolean,
    val messages: Set<MessageRow>,
) {
    companion object {
        fun from(chat: Chat) = ChatRow(
            id = chat.id?.value,
            type = chat.type,
            subject = chat.subject()?.value?.value,
            askedBy = chat.askedBy.value,
            answeredBy = chat.answeredBy()?.value,
            createdAt = chat.createdAt,
            updatedAt = chat.updatedAt(),
            isVisibleToPublic = chat.isVisibleToPublic(),
            isViewedByImam = chat.isViewedByImam(),
            isViewedByInquirer = chat.isViewedByInquirer(),
            messages = chat.messages().map { MessageRow.from(it) }.toSet(),
        )
    }

    fun toDomain(clock: Clock, eventPublisher: EventPublisher, getCurrentUser: GetCurrentUser) = Chat.restore(
        id = Chat.Id(id!!),
        type = type,
        subject = subject?.run { Subject.from(subject) },
        askedBy = User.Id(askedBy),
        answeredBy = answeredBy?.run { User.Id(answeredBy) },
        createdAt = createdAt,
        updatedAt = updatedAt,
        isVisibleToPublic = isVisibleToPublic,
        isViewedByImam = isViewedByImam,
        isViewedByInquirer = isViewedByInquirer,
        messages = messages.map { it.toDomain(clock) },

        clock = clock,
        eventPublisher = eventPublisher,
        getCurrentUser = getCurrentUser,
    )
}
