package kz.azan.askimam.favorite.app.usecase

import io.vavr.collection.Seq
import io.vavr.control.Either
import kz.azan.askimam.chat.app.usecase.GetChat
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.app.projection.FavoriteProjection
import kz.azan.askimam.favorite.domain.model.FavoriteRepository

class GetMyFavorites(
    private val currentUser: GetCurrentUser,
    private val favoriteRepository: FavoriteRepository,
    private val getChat: GetChat,
) {
    operator fun invoke(): Either<Declination, Seq<FavoriteProjection>> =
        favoriteRepository.findByUserId(currentUser().id).flatMap { list ->
            Either.sequenceRight(
                list.map { favorite ->
                    getChat(favorite.chatId).map {
                        FavoriteProjection(favorite.id!!, it.id!!, it.subjectText())
                    }
                }
            )
        }
}