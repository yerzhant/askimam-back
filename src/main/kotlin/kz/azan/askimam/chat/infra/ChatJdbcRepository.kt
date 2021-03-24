package kz.azan.askimam.chat.infra

import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.kotlin.Try
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.domain.EventPublisher
import java.time.Clock

class ChatJdbcRepository(
    private val dao: ChatDao,

    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,
) : ChatRepository {

    override fun findById(id: Chat.Id): Either<Declination, Chat> =
        Try { dao.findById(id.value) }
            .map { it.orElseThrow { Exception("Chat not found") } }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.toDomain(clock, eventPublisher, getCurrentUser) }
            )

    override fun create(chat: Chat): Option<Declination> =
        Try { dao.save(ChatRow.from(chat)) }.fold(
            { some(Declination.from(it)) },
            { none() }
        )

    override fun delete(chat: Chat): Option<Declination> =
        Try { dao.delete(ChatRow.from(chat)) }.fold(
            { some(Declination.from(it)) },
            { none() }
        )

    override fun update(chat: Chat): Option<Declination> =
        Try { dao.save(ChatRow.from(chat)) }.fold(
            { some(Declination.from(it)) },
            { none() }
        )
}