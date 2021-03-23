package kz.azan.askimam.favorite

import io.mockk.mockk
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.domain.model.FavoriteRepository
import kz.azan.askimam.favorite.infra.FavoriteDao
import kz.azan.askimam.favorite.infra.FavoriteRow
import java.time.ZonedDateTime

open class FavoriteFixtures : ChatFixtures() {

    val favoriteRepository = mockk<FavoriteRepository>()
    val favoriteDao = mockk<FavoriteDao>()

    val fixtureFavoriteId = Favorite.Id(1)
    val fixtureFavorite = Favorite(fixtureFavoriteId, fixtureInquirerId, fixtureChatId, timeAfter(0))

    val fixtureFavoriteRow = FavoriteRow(
        fixtureFavoriteId.value,
        fixtureInquirerId.value,
        fixtureChatId.value,
        timeAfter(0)
    )
}