package kz.azan.askimam.favorite.domain.policy

import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import kz.azan.askimam.favorite.domain.policy.DeleteFavoritePolicy.Companion.forAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DeleteFavoritePolicyTest : FavoriteFixtures() {

    @Test
    internal fun `should allow to delete a favorite`() {
        assertThat(forAll.isAllowed(fixtureFavorite, fixtureInquirer).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to delete a favorite`() {
        assertThat(
            forAll.isAllowed(fixtureFavorite, fixtureAnotherInquirer)
        ).isEqualTo(some(Declination.withReason("You're not allowed to delete this favorite")))
    }
}