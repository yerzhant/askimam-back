package kz.azan.askimam.favorite.app.usecase

import io.vavr.collection.Seq
import io.vavr.control.Either
import io.vavr.kotlin.right
import io.vavr.kotlin.toVavrList
import kz.azan.askimam.chat.app.usecase.GetChat
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.app.projection.FavoriteProjection
import kz.azan.askimam.favorite.domain.repo.FavoriteRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser

@UseCase
class GetMyFavorites(
    private val currentUser: GetCurrentUser,
    private val favoriteRepository: FavoriteRepository,
    private val getChat: GetChat,
) {
    operator fun invoke(): Either<Declination, Seq<FavoriteProjection>> =
        currentUser().fold(
            { right(emptyList<FavoriteProjection>().toVavrList()) },
            { user ->
                favoriteRepository.findByUserId(user.id).flatMap { list ->
                    Either.sequenceRight(
                        list.map { favorite ->
                            getChat(favorite.chatId).map {
                                FavoriteProjection(favorite.id!!, it.id, it.subject)
                            }
                        }
                    )
                }
            }
        )
}