package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import io.vavr.kotlin.left
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.repo.ChatRepository
import kz.azan.askimam.chat.domain.policy.GetChatPolicy
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.repo.UserRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser

@UseCase
class GetChat(
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
) {
    operator fun invoke(id: Chat.Id): Either<Declination, ChatProjection> = chatRepository.findById(id).flatMap {
        if (it.isVisibleToPublic()) {
            ChatProjection.from(it, userRepository)
        } else {
            getCurrentUser().fold(
                { left(Declination.withReason("Who are you?")) },
                { currentUser ->
                    GetChatPolicy
                        .getFor(currentUser)
                        .isAllowed(it, currentUser).flatMap { chat -> ChatProjection.from(chat, userRepository) }
                }
            )
        }
    }
}
