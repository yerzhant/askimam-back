package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.policy.DeleteChatPolicy
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.service.GetCurrentUser

@UseCase
class DeleteChat(
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
) {

    operator fun invoke(id: Chat.Id): Option<Declination> = getCurrentUser().fold(
        { some(Declination.withReason("Who are you?")) },
        { currentUser ->
            val policy = DeleteChatPolicy.getFor(currentUser)

            chatRepository.findById(id)
                .fold(
                    { some(it) },
                    { policy.isAllowed(it, currentUser).orElse { chatRepository.delete(it) } }
                )
        }
    )
}