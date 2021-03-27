package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.policy.GetChatPolicy
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.UserRepository

@UseCase
class GetChat(
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
) {
    operator fun invoke(id: Chat.Id): Either<Declination, ChatProjection> {
        val currentUser = getCurrentUser()
        val policy = GetChatPolicy.getFor(currentUser)

        return chatRepository.findById(id).flatMap {
            policy.isAllowed(it, currentUser).flatMap { chat -> ChatProjection.from(chat, userRepository) }
        }
    }
}