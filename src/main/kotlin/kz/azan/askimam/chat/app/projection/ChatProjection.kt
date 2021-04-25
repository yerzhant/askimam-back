package kz.azan.askimam.chat.app.projection

import io.vavr.collection.Seq
import io.vavr.control.Either
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.UserRepository

data class ChatProjection(
    val id: Chat.Id,
    val type: Chat.Type,
    val askedBy: User.Id,
    val subject: Subject,
    val isFavorite: Boolean = false,
    var messages: Seq<MessageProjection>? = null,
) {
    companion object {
        fun from(chat: Chat, userRepository: UserRepository): Either<Declination, ChatProjection> =
            Either.sequenceRight(
                chat.messages().map { MessageProjection.from(it, userRepository) }
            ).map {
                ChatProjection(
                    chat.id!!,
                    chat.type,
                    chat.askedBy,
                    chat.subjectText(),
                    messages = it,
                )
            }
    }
}