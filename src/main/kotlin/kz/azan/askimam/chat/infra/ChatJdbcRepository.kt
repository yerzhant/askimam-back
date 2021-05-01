package kz.azan.askimam.chat.infra

import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.kotlin.Try
import io.vavr.kotlin.failure
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import kz.azan.askimam.user.domain.service.GetCurrentUser
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class ChatJdbcRepository(
    private val dao: ChatDao,

    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,
) : ChatRepository {

    override fun findById(id: Chat.Id): Either<Declination, Chat> =
        Try { dao.findById(id.value) }
            .map { it.orElseThrow { Exception("The chat is not found") } }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.toDomain(clock, eventPublisher, getCurrentUser) }
            )

    override fun findPublicChats(offset: Int, pageSize: Int): Either<Declination, List<Chat>> =
        Try { dao.findByTypeAndIsVisibleToPublicIsTrueOrderByUpdatedAtDesc(Public, PageRequest.of(offset, pageSize)) }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.map { row -> row.toDomain(clock, eventPublisher, getCurrentUser) } }
            )

    override fun findMyChats(offset: Int, pageSize: Int): Either<Declination, List<Chat>> =
        Try {
            getCurrentUser().fold(
                { failure(Exception("Who are you?")) },
                { user ->
                    when (user.type) {
                        Imam -> dao.findByAnsweredByOrderByUpdatedAtDesc(
                            user.id.value,
                            PageRequest.of(offset, pageSize)
                        )
                        Inquirer -> dao.findByAskedByOrderByUpdatedAtDesc(
                            user.id.value,
                            PageRequest.of(offset, pageSize)
                        )
                    }
                }
            )
        }.toEither()
            .bimap(
                { Declination.from(it) },
                { it.map { row -> row.toDomain(clock, eventPublisher, getCurrentUser) } }
            )

    override fun findUnansweredChats(offset: Int, pageSize: Int): Either<Declination, List<Chat>> =
        Try { dao.findByAnsweredByIsNullOrderByUpdatedAtDesc(PageRequest.of(offset, pageSize)) }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.map { row -> row.toDomain(clock, eventPublisher, getCurrentUser) } }
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