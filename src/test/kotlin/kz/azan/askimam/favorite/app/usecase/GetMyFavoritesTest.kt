package kz.azan.askimam.favorite.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetMyFavoritesTest : FavoriteFixtures() {

    @Test
    internal fun `should return favorites`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { userRepository.findById(fixtureImamId) } returns right(fixtureImam)
        every { favoriteRepository.findByUserId(fixtureInquirerId) } returns right(listOfFavoritesFixture)
        every { getChat(any()) } returns ChatProjection.from(fixtureSavedChat(), userRepository)

        val list = GetMyFavorites(getCurrentUser, favoriteRepository, getChat)().get()

        assertThat(list).hasSize(2)
        assertThat(list.first().id).isEqualTo(fixtureFavorite.id)
        assertThat(list.first().chatId).isEqualTo(fixtureChatId1)
        assertThat(list.first().subject).isEqualTo(fixtureSubject)
    }

    @Test
    internal fun `should fail on a second chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { userRepository.findById(fixtureImamId) } returns right(fixtureImam)
        every { favoriteRepository.findByUserId(fixtureInquirerId) } returns right(listOfFavoritesFixture)
        every { getChat(any()) } returnsMany listOf(
            ChatProjection.from(fixtureSavedChat(), userRepository),
            left(Declination.withReason("db error"))
        )

        assertThat(GetMyFavorites(getCurrentUser, favoriteRepository, getChat)().left)
            .isEqualTo(Declination.withReason("db error"))
    }

    @Test
    internal fun `should not return favorites`() {
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { favoriteRepository.findByUserId(fixtureInquirerId) } returns left(Declination.withReason("boom!"))

        assertThat(GetMyFavorites(getCurrentUser, favoriteRepository, getChat)().left)
            .isEqualTo(Declination.withReason("boom!"))
    }
}