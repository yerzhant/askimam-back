package kz.azan.askimam.favorite.infra

import io.mockk.every
import io.mockk.verify
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.FavoriteFixtures
import kz.azan.askimam.favorite.domain.model.Favorite
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FavoriteJdbcRepositoryTest : FavoriteFixtures() {

    @Test
    internal fun `should find by a user id`() {
        val favorites = setOf(fixtureFavoriteRow)
        every { favoriteDao.findByUserId(fixtureInquirerId.value) } returns favorites

        assertThat(FavoriteJdbcRepository(favoriteDao).findByUserId(fixtureInquirerId).get())
            .isEqualTo(favorites.map { it.toDomain() })
    }

    @Test
    internal fun `should return empty list`() {
        every { favoriteDao.findByUserId(fixtureInquirerId.value) } returns emptySet()

        assertThat(FavoriteJdbcRepository(favoriteDao).findByUserId(fixtureInquirerId).get())
            .isEqualTo(emptyList<Favorite>())
    }

    @Test
    internal fun `should not find by a user id - db exception`() {
        every { favoriteDao.findByUserId(fixtureInquirerId.value) } throws Exception("ta-da")

        assertThat(FavoriteJdbcRepository(favoriteDao).findByUserId(fixtureInquirerId).left)
            .isEqualTo(Declination.withReason("ta-da"))
    }

    @Test
    internal fun `should not find by a user id - empty message`() {
        every { favoriteDao.findByUserId(fixtureInquirerId.value) } throws Exception("")

        assertThat(FavoriteJdbcRepository(favoriteDao).findByUserId(fixtureInquirerId).left)
            .isEqualTo(Declination.withReason("Unknown"))
    }

    @Test
    internal fun `should not find by a user id - null message`() {
        every { favoriteDao.findByUserId(fixtureInquirerId.value) } throws Exception()

        assertThat(FavoriteJdbcRepository(favoriteDao).findByUserId(fixtureInquirerId).left)
            .isEqualTo(Declination.withReason("Unknown"))
    }

    @Test
    internal fun `should find by a user id and chat id`() {
        every {
            favoriteDao.findByUserIdAndChatId(fixtureInquirerId.value, fixtureChatId.value)
        } returns fixtureFavoriteRow

        assertThat(FavoriteJdbcRepository(favoriteDao).findByUserIdAndChatId(fixtureInquirerId, fixtureChatId).get())
            .isEqualTo(fixtureFavorite)
    }

    @Test
    internal fun `should not find by a user id and chat id`() {
        every {
            favoriteDao.findByUserIdAndChatId(fixtureInquirerId.value, fixtureChatId.value)
        } throws Exception("boom")

        assertThat(FavoriteJdbcRepository(favoriteDao).findByUserIdAndChatId(fixtureInquirerId, fixtureChatId).left)
            .isEqualTo(Declination.withReason("boom"))
    }

    @Test
    internal fun `should add a new favorite`() {
        every { favoriteDao.save(fixtureFavoriteRow) } returns fixtureFavoriteRow

        assertThat(FavoriteJdbcRepository(favoriteDao).add(fixtureFavorite).isEmpty).isTrue

        verify { favoriteDao.save(fixtureFavoriteRow) }
    }

    @Test
    internal fun `should not add a new favorite`() {
        every { favoriteDao.save(fixtureFavoriteRow) } throws Exception("Hi")

        assertThat(
            FavoriteJdbcRepository(favoriteDao).add(fixtureFavorite).get()
        ).isEqualTo(Declination.withReason("Hi"))
    }

    @Test
    internal fun `should delete a favorite`() {
        every { favoriteDao.delete(fixtureFavoriteRow) } returns Unit

        assertThat(FavoriteJdbcRepository(favoriteDao).delete(fixtureFavorite).isEmpty).isTrue

        verify { favoriteDao.delete(fixtureFavoriteRow) }
    }

    @Test
    internal fun `should not delete a favorite`() {
        every { favoriteDao.delete(fixtureFavoriteRow) } throws Exception("Hello")

        assertThat(FavoriteJdbcRepository(favoriteDao).delete(fixtureFavorite).get()).isEqualTo(
            Declination.withReason("Hello")
        )
    }
}