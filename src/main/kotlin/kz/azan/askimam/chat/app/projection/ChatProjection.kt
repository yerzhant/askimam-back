package kz.azan.askimam.chat.app.projection

import io.vavr.collection.Seq
import io.vavr.control.Either
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.repo.UserRepository
import java.time.LocalDateTime

data class ChatProjection(
    val id: Chat.Id,
    val type: Chat.Type,
    val askedBy: User.Id,
    val subject: Subject,
    val updatedAt: LocalDateTime,
    val isFavorite: Boolean = false,
    val isViewedByImam: Boolean = false,
    val isViewedByInquirer: Boolean = false,
    var messages: Seq<MessageProjection>? = null,
) {
    companion object {
        fun from(chat: Chat, userRepository: UserRepository): Either<Declination, ChatProjection> =
            Either.sequenceRight(
                chat.messages().map { MessageProjection.from(it, userRepository) }
            ).map {
                ChatProjection(
                    id = chat.id!!,
                    type = chat.type,
                    askedBy = chat.askedBy,
                    subject = chat.subjectText(),
                    updatedAt = chat.updatedAt(),
                    isViewedByImam = chat.isViewedByImam(),
                    isViewedByInquirer = chat.isViewedByInquirer(),
                    messages = it,
                )
            }
    }
}
