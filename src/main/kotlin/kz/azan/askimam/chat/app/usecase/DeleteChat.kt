package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.policy.DeleteChatPolicy
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination

class DeleteChat(
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
) {

    operator fun invoke(id: Chat.Id): Option<Declination> {
        val currentUser = getCurrentUser()
        val policy = DeleteChatPolicy.getFor(currentUser)

        return chatRepository.findById(id)
            .fold(
                { some(it) },
                { policy.isAllowed(it, currentUser).orElse { chatRepository.delete(it) } }
            )
    }
}