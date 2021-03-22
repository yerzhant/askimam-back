package kz.azan.askimam.favorite

import io.mockk.mockk
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.favorite.domain.model.FavoriteRepository

open class FavoriteFixtures : ChatFixtures(){

    val favoriteRepository = mockk<FavoriteRepository>()
}