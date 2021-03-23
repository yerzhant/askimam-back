package kz.azan.askimam.chat.app.usecase

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NonBlankString
import java.time.Clock

class CreateChat(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(type: Type, text: NonBlankString) = chatRepository.create(
        Chat.new(
            clock,
            eventPublisher,
            getCurrentUser,
            type,
            text,
        )
    )

    fun withSubject(type: Type, subject: Subject, text: NonBlankString) = chatRepository.create(
        Chat.newWithSubject(
            clock,
            eventPublisher,
            getCurrentUser,
            type,
            subject,
            text,
        )
    )
}