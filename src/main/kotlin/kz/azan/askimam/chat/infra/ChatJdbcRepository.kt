package kz.azan.askimam.chat.infra

import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.kotlin.Try
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.domain.EventPublisher
import java.time.Clock

class ChatJdbcRepository(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,
    private val dao: ChatDao
) : ChatRepository {

    override fun findById(id: Chat.Id): Either<Declination, Chat> =
        Try { dao.findById(id.value) }
            .map { it.orElseThrow { Exception("Chat not found") } }
            .toEither()
            .bimap(
                { Declination.withReason(it.message) },
                { it.toDomain(clock, eventPublisher, getCurrentUser) }
            )

    override fun create(chat: Chat): Option<Declination> {
        TODO("Not yet implemented")
    }

    override fun delete(chat: Chat): Option<Declination> {
        TODO("Not yet implemented")
    }

    override fun update(chat: Chat): Option<Declination> {
        TODO("Not yet implemented")
    }
}