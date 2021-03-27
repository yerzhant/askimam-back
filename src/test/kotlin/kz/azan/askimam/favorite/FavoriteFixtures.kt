package kz.azan.askimam.favorite

import io.mockk.mockk
import io.vavr.kotlin.toVavrList
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.favorite.app.projection.FavoriteProjection
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.domain.model.FavoriteRepository
import kz.azan.askimam.favorite.infra.FavoriteDao
import kz.azan.askimam.favorite.infra.FavoriteRow

open class FavoriteFixtures : ChatFixtures() {

    val favoriteRepository = mockk<FavoriteRepository>()
    val favoriteDao = mockk<FavoriteDao>()

    private val fixtureFavoriteId = Favorite.Id(1)
    val fixtureFavorite = Favorite(fixtureFavoriteId, fixtureInquirerId, fixtureChatId1, timeAfter(0))

    val fixtureFavoriteRow = FavoriteRow(
        fixtureFavoriteId.value,
        fixtureInquirerId.value,
        fixtureChatId1.value,
        timeAfter(0)
    )

    val listOfFavoritesFixture = listOf(
        fixtureFavorite,
        fixtureFavorite.copy(Favorite.Id(10)),
    )

    val sequenceOfFavoriteProjectionsFixture = listOfFavoritesFixture.map {
        FavoriteProjection(it.id!!, it.chatId, Subject.from("Subject ${it.id?.value}"))
    }.toVavrList()
}