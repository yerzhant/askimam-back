package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetMyChatsTest : FavoriteFixtures() {

    @Test
    internal fun `should get my chats`() {
        fixtureClock()
        every { getMyFavorites() } returns right(sequenceOfFavoriteProjectionsFixture)
        every { chatRepository.findMyChats(0, 20) } returns right(fixtureSavedTwoChats().reversed())

        val list = GetMyChats(chatRepository, getMyFavorites)(0, 20).get()

        assertThat(list).hasSize(2)
    }

    @Test
    internal fun `should not get my chats`() {
        fixtureClock()
        every { getMyFavorites() } returns right(sequenceOfFavoriteProjectionsFixture)
        every { chatRepository.findMyChats(0, 20) } returns left(Declination.withReason("x"))

        assertThat(GetMyChats(chatRepository, getMyFavorites)(0, 20).isLeft).isTrue
    }
}