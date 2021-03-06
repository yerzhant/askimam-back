package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import io.vavr.kotlin.left
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.policy.GetUnansweredChatsPolicy
import kz.azan.askimam.chat.domain.repo.ChatRepository
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.service.GetCurrentUser

@UseCase
class GetUnansweredChats(
    private val chatRepository: ChatRepository,
    private val getCurrentUser: GetCurrentUser,
) {

    operator fun invoke(offset: Int, pageSize: Int): Either<Declination, List<ChatProjection>> = getCurrentUser().fold(
        { left(Declination.withReason("Who are you?")) },
        { user ->
            GetUnansweredChatsPolicy.forAll.isAllowed(user).fold(
                {
                    chatRepository.findUnansweredChats(offset, pageSize).map { chats ->
                        chats.map { chat ->
                            ChatProjection(
                                id = chat.id!!,
                                type = chat.type,
                                askedBy = chat.askedBy,
                                subject = chat.subjectText(),
                                updatedAt = chat.updatedAt(),
                                isViewedByImam = chat.isViewedByImam(),
                                isViewedByInquirer = chat.isViewedByInquirer(),
                            )
                        }
                    }
                },
                { left(it) }
            )
        }
    )
}