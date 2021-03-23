package kz.azan.askimam.favorite.infra

import kz.azan.askimam.favorite.FavoriteFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Sql("/scripts/users.sql", "/scripts/chat.sql", "/scripts/favorites.sql")
class FavoriteJdbcRepositoryIT(
    private val dao: FavoriteDao
) : FavoriteFixtures() {

    @Test
    internal fun `should get list of favorites`() {
        val favorites = dao.findByUserId(fixtureFavoriteRow.userId)

        assertThat(favorites).hasSize(1)
        assertThat(favorites.first().id).isEqualTo(1)
        assertThat(favorites.first().userId).isEqualTo(2)
        assertThat(favorites.first().chatId).isEqualTo(1)
        assertThat(favorites.first().addedAt).isCloseTo(
            LocalDateTime.now(),
            TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    internal fun `should get a favorite`() {
        val favorite = dao.findByUserIdAndChatId(fixtureFavoriteRow.userId, fixtureChatId.value)

        assertThat(favorite.id).isEqualTo(1)
        assertThat(favorite.userId).isEqualTo(2)
        assertThat(favorite.chatId).isEqualTo(1)
        assertThat(favorite.addedAt).isCloseTo(
            LocalDateTime.now(),
            TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    internal fun `should add a favorite`() {
        dao.save(FavoriteRow(null, 2, 2, LocalDateTime.now()))

        val favoriteRow = dao.findByUserIdAndChatId(2, 2)
        assertThat(favoriteRow.id).isNotNull
        assertThat(dao.findAll()).hasSize(2)
    }

    @Test
    internal fun `should not add a duplicate favorite`() {
        assertThrows<DbActionExecutionException> {
            dao.save(FavoriteRow(null, 2, 1, LocalDateTime.now()))
        }

        assertThat(dao.findAll()).hasSize(1)
    }

    @Test
    internal fun `should delete a favorite`() {
        dao.delete(fixtureFavoriteRow)

        assertThat(dao.findAll()).hasSize(0)
    }
}