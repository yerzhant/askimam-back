package kz.azan.askimam.chat.app.projection

import io.vavr.control.Either
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.UserRepository
import java.time.LocalDateTime

data class MessageProjection(
    val id: Message.Id,
    val type: Message.Type,
    val text: NonBlankString,
    val author: User?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(message: Message, userRepository: UserRepository): Either<Declination, MessageProjection> =
            getUserIfNecessary(message, userRepository).map {
                MessageProjection(
                    message.id!!,
                    message.type,
                    message.text(),
                    it,
                    message.createdAt,
                    message.updatedAt(),
                )
            }

        private fun getUserIfNecessary(
            message: Message,
            userRepository: UserRepository
        ) = if (message.authorType == Imam) userRepository.findById(message.authorId) else right(null)
    }
}
