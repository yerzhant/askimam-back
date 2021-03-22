package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.app.service.GetCurrentUser
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.model.MessageRepository
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NonBlankString
import java.time.Clock

class CreateChat(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
) {
    operator fun invoke(
        type: Type,
        text: NonBlankString,
    ): Option<Declination> = Chat.new(
        clock,
        eventPublisher,
        getCurrentUser,
        chatRepository,
        messageRepository,
        type,
        text,
    ).fold(
        { some(it) },
        { none() }
    )

    fun withSubject(
        type: Type,
        subject: Subject,
        text: NonBlankString,
    ): Option<Declination> = Chat.newWithSubject(
        clock,
        eventPublisher,
        getCurrentUser,
        chatRepository,
        messageRepository,
        type,
        subject,
        text,
    ).fold(
        { some(it) },
        { none() }
    )
}