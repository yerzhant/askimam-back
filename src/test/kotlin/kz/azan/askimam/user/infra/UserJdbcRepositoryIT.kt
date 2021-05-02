package kz.azan.askimam.user.infra

import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.meta.DataJdbcIT
import kz.azan.askimam.user.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@DataJdbcIT
@Sql("/scripts/users.sql")
class UserJdbcRepositoryIT(
    private val dao: UserDao,
    private val fcmTokenDao: FcmTokenDao,
) : ChatFixtures() {

    private val repository = UserJdbcRepository(dao, fcmTokenDao)

    @Test
    internal fun `raw access`() {
        dao.findById(1).get()
    }

    @Test
    internal fun `should find an imam`() {
        val user = repository.findById(fixtureImamId).get()

        assertThat(user.id).isEqualTo(fixtureImam.id)
        assertThat(user.type).isEqualTo(fixtureImam.type)
        assertThat(user.name).isEqualTo(NonBlankString.of("The Imam"))
    }

    @Test
    internal fun `should find an inquirer`() {
        val user = repository.findById(fixtureInquirerId).get()

        assertThat(user.id).isEqualTo(fixtureInquirer.id)
        assertThat(user.type).isEqualTo(fixtureInquirer.type)
        assertThat(user.name).isEqualTo(NonBlankString.of("Jon Dow"))
    }

    @Test
    internal fun `should not find a user`() {
        assertThat(repository.findById(User.Id(100)).isLeft).isTrue
    }

    @Test
    internal fun `should find by username and status`() {
        assertThat(repository.findByUsernameAndStatus("the-imam", 1).isRight).isTrue
    }

    @Test
    internal fun `should not find by username and status`() {
        assertThat(repository.findByUsernameAndStatus("the-imam", 0).isLeft).isTrue
    }

    @Test
    internal fun `should save tokens`() {
        val user = User(
            id = fixtureInquirerId,
            type = fixtureInquirer.type,
            name = fixtureInquirer.name,
            passwordHash = fixtureInquirer.passwordHash,
            fcmTokens = setOf(fixtureInquirerFcmToken).toMutableSet(),
        )

        repository.saveTokens(user)

        val foundTokens = fcmTokenDao.findByUserId(fixtureInquirerId.value)
        assertThat(foundTokens).hasSize(1)
        assertThat(foundTokens.first().value).isEqualTo(fixtureInquirerFcmToken.value.value)
    }
}
