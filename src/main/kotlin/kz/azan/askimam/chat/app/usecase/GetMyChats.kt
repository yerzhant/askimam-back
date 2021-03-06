package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.repo.ChatRepository
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.app.usecase.GetMyFavorites

@UseCase
class GetMyChats(
    private val chatRepository: ChatRepository,
    private val getMyFavorites: GetMyFavorites,
) {

    operator fun invoke(offset: Int, pageSize: Int): Either<Declination, List<ChatProjection>> =
        getMyFavorites().flatMap { favorites ->
            chatRepository.findMyChats(offset, pageSize).map { chats ->
                chats.map { chat ->
                    ChatProjection(
                        id = chat.id!!,
                        type = chat.type,
                        askedBy = chat.askedBy,
                        subject = chat.subjectText(),
                        updatedAt = chat.updatedAt(),
                        isViewedByImam = chat.isViewedByImam(),
                        isViewedByInquirer = chat.isViewedByInquirer(),
                        isFavorite = favorites.map { it.chatId }.contains(chat.id),
                    )
                }
            }
        }
}
