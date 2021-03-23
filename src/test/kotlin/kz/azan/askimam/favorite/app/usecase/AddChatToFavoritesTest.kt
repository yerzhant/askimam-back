package kz.azan.askimam.favorite.app.usecase

import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AddChatToFavoritesTest : FavoriteFixtures() {

    @Test
    internal fun `should add a favorite`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.add(fixtureFavorite.copy(null)) } returns none()

        assertThat(AddChatToFavorites(clock, getCurrentUser, favoriteRepository)(fixtureChatId).isEmpty).isTrue
    }

    @Test
    internal fun `should not add a favorite`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.add(fixtureFavorite.copy(null)) } returns some(Declination.withReason("boom!"))

        assertThat(AddChatToFavorites(clock, getCurrentUser, favoriteRepository)(fixtureChatId)).isEqualTo(
            some(Declination.withReason("boom!"))
        )
    }
}