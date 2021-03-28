package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Either
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.ChatRepository
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.app.usecase.GetMyFavorites

@UseCase
class GetPublicChats(
    private val chatRepository: ChatRepository,
    private val getMyFavorites: GetMyFavorites,
) {

    operator fun invoke(offset: Int, pageSize: Int): Either<Declination, List<ChatProjection>> =
        getMyFavorites().flatMap { favorites ->
            chatRepository.findPublicChats(offset, pageSize).map { chats ->
                chats.map { chat ->
                    ChatProjection(
                        chat.id!!,
                        chat.subjectText(),
                        favorites.map { it.chatId }.contains(chat.id),
                    )
                }
            }
        }
}