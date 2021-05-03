package kz.azan.askimam.favorite.domain.repo

import io.vavr.control.Either
import io.vavr.control.Option
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.user.domain.model.User

interface FavoriteRepository {
    fun findByUserId(id: User.Id): Either<Declination, List<Favorite>>
    fun findByUserIdAndChatId(userId: User.Id, chatId: Chat.Id): Either<Declination, Favorite>
    fun add(favorite: Favorite): Option<Declination>
    fun delete(favorite: Favorite): Option<Declination>
}