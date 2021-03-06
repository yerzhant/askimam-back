package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type
import kz.azan.askimam.chat.domain.repo.ChatRepository
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.event.domain.service.EventPublisher
import kz.azan.askimam.user.domain.service.GetCurrentUser
import java.time.Clock

@UseCase
class CreateChat(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(type: Type, text: NonBlankString, fcmToken: FcmToken): Option<Declination> =
        Chat.new(
            type = type,
            messageText = text,

            inquirerFcmToken = fcmToken,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        ).fold(
            { some(it) },
            { chatRepository.create(it) }
        )

    fun withSubject(type: Type, subject: Subject, text: NonBlankString, fcmToken: FcmToken): Option<Declination> =
        Chat.newWithSubject(
            type = type,
            subject = subject,
            messageText = text,

            inquirerFcmToken = fcmToken,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        ).fold(
            { some(it) },
            { chatRepository.create(it) }
        )
}