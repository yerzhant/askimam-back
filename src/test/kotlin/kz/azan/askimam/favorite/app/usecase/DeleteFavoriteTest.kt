package kz.azan.askimam.favorite.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DeleteFavoriteTest : FavoriteFixtures() {

    @Test
    internal fun `should delete a favorite`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.findByUserIdAndChatId(fixtureInquirerId, fixtureChatId) } returns right(
            fixtureFavorite
        )
        every { favoriteRepository.delete(fixtureFavorite) } returns none()

        assertThat(DeleteFavorite(getCurrentUser, favoriteRepository)(fixtureChatId).isEmpty).isTrue
    }

    @Test
    internal fun `should not delete a favorite - no such chat`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.findByUserIdAndChatId(fixtureInquirerId, fixtureChatId) } returns left(
            Declination.withReason("oops")
        )

        assertThat(DeleteFavorite(getCurrentUser, favoriteRepository)(fixtureChatId)).isEqualTo(
            some(Declination.withReason("oops"))
        )
    }

    @Test
    internal fun `should not delete a favorite - db error`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.findByUserIdAndChatId(fixtureInquirerId, fixtureChatId) } returns right(
            fixtureFavorite
        )
        every { favoriteRepository.delete(fixtureFavorite) } returns some(Declination.withReason("error"))

        assertThat(DeleteFavorite(getCurrentUser, favoriteRepository)(fixtureChatId)).isEqualTo(
            some(Declination.withReason("error"))
        )
    }

    @Test
    internal fun `should not delete a favorite (mistake in infra - policy in action)`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { favoriteRepository.findByUserIdAndChatId(fixtureInquirerId, fixtureChatId) } returns right(
            fixtureFavorite.copy(userId = fixtureAnotherInquirer.id)
        )

        assertThat(DeleteFavorite(getCurrentUser, favoriteRepository)(fixtureChatId).isDefined).isTrue
    }
}