package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import io.vavr.kotlin.left
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.chat.domain.policy.GetUnansweredChatsPolicy
import kz.azan.askimam.user.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination

class GetUnansweredChats(
    private val chatRepository: ChatRepository,
    private val getCurrentUser: GetCurrentUser,
) {

    operator fun invoke(offset: Int, pageSize: Int): Either<Declination, List<ChatProjection>> =
        GetUnansweredChatsPolicy.forAll.isAllowed(getCurrentUser()).fold(
            {
                chatRepository.findUnansweredChats(offset, pageSize).map { chats ->
                    chats.map { chat ->
                        ChatProjection(chat.id!!, chat.subjectText())
                    }
                }
            },
            { left(it) }
        )
}