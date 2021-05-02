package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import io.vavr.kotlin.toVavrList
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import kz.azan.askimam.favorite.app.projection.FavoriteProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetPublicChatsTest : FavoriteFixtures() {

    @Test
    internal fun `should return a list of projections of public chats`() {
        fixtureClock()
        every { getMyFavorites() } returns right(sequenceOfFavoriteProjectionsFixture)
        every { chatRepository.findPublicChats(0, 20) } returns right(fixtureSavedTwoChats().reversed())

        val list = GetPublicChats(chatRepository, getMyFavorites)(0, 20).get()

        assertThat(list).hasSize(2)

        assertThat(list.first().id).isEqualTo(fixtureChatId2)
        assertThat(list.first().subject).isEqualTo(Subject(fixtureMessage))
        assertThat(list.first().isFavorite).isFalse

        assertThat(list.last().id).isEqualTo(fixtureChatId1)
        assertThat(list.last().subject).isEqualTo(fixtureSubject)
        assertThat(list.last().isFavorite).isTrue
    }

    @Test
    internal fun `should return a list of projections of public chats - chats are empty`() {
        fixtureClock()
        every { getMyFavorites() } returns right(sequenceOfFavoriteProjectionsFixture)
        every { chatRepository.findPublicChats(0, 20) } returns right(emptyList())

        val list = GetPublicChats(chatRepository, getMyFavorites)(0, 20).get()

        assertThat(list).hasSize(0)
    }

    @Test
    internal fun `should not return a list of projections of public chats - chat repo error`() {
        fixtureClock()
        every { getMyFavorites() } returns right(sequenceOfFavoriteProjectionsFixture)
        every { chatRepository.findPublicChats(0, 20) } returns left(Declination.withReason("x"))

        assertThat(GetPublicChats(chatRepository, getMyFavorites)(0, 20).isLeft).isTrue
    }

    @Test
    internal fun `should not return a list of projections of public chats - favorites error`() {
        fixtureClock()
        every { getMyFavorites() } returns left(Declination.withReason("x"))

        assertThat(GetPublicChats(chatRepository, getMyFavorites)(0, 20).isLeft).isTrue
    }

    @Test
    internal fun `should return a list of projections of public chats - favorites are empty`() {
        fixtureClock()
        every { getMyFavorites() } returns right(listOf<FavoriteProjection>().toVavrList())
        every { chatRepository.findPublicChats(0, 20) } returns right(fixtureSavedTwoChats().reversed())

        val list = GetPublicChats(chatRepository, getMyFavorites)(0, 20).get()

        assertThat(list).hasSize(2)

        assertThat(list.first().id).isEqualTo(fixtureChatId2)
        assertThat(list.first().subject).isEqualTo(Subject(fixtureMessage))
        assertThat(list.first().isFavorite).isFalse

        assertThat(list.last().id).isEqualTo(fixtureChatId1)
        assertThat(list.last().subject).isEqualTo(fixtureSubject)
        assertThat(list.last().isFavorite).isFalse
    }
}