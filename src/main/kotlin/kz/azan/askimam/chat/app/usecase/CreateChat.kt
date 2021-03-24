package kz.azan.askimam.chat.app.usecase

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.model.FcmToken
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
    operator fun invoke(type: Type, text: NonBlankString, fcmToken: FcmToken) = chatRepository.create(
        Chat.new(
            type = type,
            messageText = text,

            inquirerFcmToken = fcmToken,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        )
    )

    fun withSubject(type: Type, subject: Subject, text: NonBlankString, fcmToken: FcmToken) = chatRepository.create(
        Chat.newWithSubject(
            type = type,
            subject = subject,
            messageText = text,

            inquirerFcmToken = fcmToken,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        )
    )
}