package kz.azan.askimam.favorite.infra.repo

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.favorite.FavoriteFixtures
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.infra.dao.FavoriteDao
import kz.azan.askimam.favorite.infra.model.FavoriteRow
import kz.azan.askimam.meta.DataJdbcIT
import kz.azan.askimam.user.domain.model.User
import org.assertj.core.api.Assertions
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DataJdbcIT
@Sql("/scripts/users.sql", "/scripts/chats.sql", "/scripts/favorites.sql")
class FavoriteJdbcRepositoryIT(
    private val dao: FavoriteDao,
) : FavoriteFixtures() {

    private val repository = FavoriteJdbcRepository(dao)

    @Test
    internal fun `should get list of favorites`() {
        val favorites = dao.findByUserId(fixtureFavoriteRow.userId)

        Assertions.assertThat(favorites).hasSize(1)
        Assertions.assertThat(favorites.first().id).isEqualTo(1)
        Assertions.assertThat(favorites.first().userId).isEqualTo(2)
        Assertions.assertThat(favorites.first().chatId).isEqualTo(1)
        Assertions.assertThat(favorites.first().addedAt).isCloseTo(
            LocalDateTime.now(),
            TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    internal fun `should get list of favorites - repo`() {
        val list = repository.findByUserId(fixtureFavorite.userId).get()

        Assertions.assertThat(list).hasSize(1)
        Assertions.assertThat(list.first().id).isEqualTo(Favorite.Id(1))
        Assertions.assertThat(list.first().userId).isEqualTo(User.Id(2))
        Assertions.assertThat(list.first().chatId).isEqualTo(Chat.Id(1))
        Assertions.assertThat(list.first().addedAt).isCloseTo(
            LocalDateTime.now(),
            TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    internal fun `should get a favorite`() {
        val favorite = dao.findByUserIdAndChatId(fixtureFavoriteRow.userId, fixtureChatId1.value)

        Assertions.assertThat(favorite.id).isEqualTo(1)
        Assertions.assertThat(favorite.userId).isEqualTo(2)
        Assertions.assertThat(favorite.chatId).isEqualTo(1)
        Assertions.assertThat(favorite.addedAt).isCloseTo(
            LocalDateTime.now(),
            TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    internal fun `should get a favorite - repo`() {
        val favorite = repository.findByUserIdAndChatId(fixtureFavorite.userId, fixtureChatId1).get()

        Assertions.assertThat(favorite.id).isEqualTo(Favorite.Id(1))
        Assertions.assertThat(favorite.userId).isEqualTo(User.Id(2))
        Assertions.assertThat(favorite.chatId).isEqualTo(Chat.Id(1))
        Assertions.assertThat(favorite.addedAt).isCloseTo(
            LocalDateTime.now(),
            TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    internal fun `should add a favorite`() {
        dao.save(FavoriteRow(null, 2, 2, LocalDateTime.now()))

        val favoriteRow = dao.findByUserIdAndChatId(2, 2)
        Assertions.assertThat(favoriteRow.id).isNotNull
        Assertions.assertThat(dao.findAll()).hasSize(2)
    }

    @Test
    internal fun `should add a favorite - repo`() {
        repository.add(Favorite(null, User.Id(2), Chat.Id(2), LocalDateTime.now()))

        val favoriteRow = dao.findByUserIdAndChatId(2, 2)
        Assertions.assertThat(favoriteRow.id).isNotNull
        Assertions.assertThat(dao.findAll()).hasSize(2)
    }

    @Test
    internal fun `should not add a duplicate favorite`() {
        assertThrows<DbActionExecutionException> {
            dao.save(FavoriteRow(null, 2, 1, LocalDateTime.now()))
        }

        Assertions.assertThat(dao.findAll()).hasSize(1)
    }

    @Test
    internal fun `should not add a duplicate favorite - repo`() {
        val option = repository.add(Favorite(null, User.Id(2), Chat.Id(1), LocalDateTime.now()))

        Assertions.assertThat(option.isDefined).isTrue
        Assertions.assertThat(dao.findAll()).hasSize(1)
    }

    @Test
    internal fun `should delete a favorite`() {
        dao.delete(fixtureFavoriteRow)

        Assertions.assertThat(dao.findAll()).hasSize(0)
    }

    @Test
    internal fun `should delete a favorite - repo`() {
        repository.delete(fixtureFavorite)

        Assertions.assertThat(dao.findAll()).hasSize(0)
    }
}