package kz.azan.askimam.favorite.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import kz.azan.askimam.favorite.domain.model.Favorite
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetMyFavoritesTest : FavoriteFixtures() {

    @Test
    internal fun `should return favorites`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.findByUserId(fixtureInquirerId) } returns right(
            listOf(
                Favorite(fixtureInquirerId, fixtureChatId, timeAfter(0)),
                Favorite(fixtureInquirerId, fixtureChatId, timeAfter(10)),
            )
        )

        assertThat(GetMyFavorites(getCurrentUser, favoriteRepository)().get()).hasSize(2)
    }

    @Test
    internal fun `should not return favorites`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.findByUserId(fixtureInquirerId) } returns left(Declination.withReason("boom!"))

        assertThat(GetMyFavorites(getCurrentUser, favoriteRepository)())
            .isEqualTo(left<Declination, List<Favorite>>(Declination.withReason("boom!")))
    }
}