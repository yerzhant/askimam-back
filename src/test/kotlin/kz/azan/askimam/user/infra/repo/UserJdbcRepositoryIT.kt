package kz.azan.askimam.user.infra.repo

import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.meta.DataJdbcIT
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.infra.dao.FcmTokenDao
import kz.azan.askimam.user.infra.dao.UserDao
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
        repository.saveTokens(fixtureInquirer)

        val foundTokens = fcmTokenDao.findByUserId(fixtureInquirerId.value)
        assertThat(foundTokens).hasSize(2)
        assertThat(foundTokens.first { it.value == fixtureInquirerFcmToken.value.value }).isNotNull
    }

    @Test
    internal fun `should delete a token`() {
        assertThat(fcmTokenDao.findByUserId(fixtureInquirerId.value)).hasSize(1)

        repository.deleteToken(FcmToken(NonBlankString.of("456x")), fixtureInquirer)

        assertThat(fcmTokenDao.findByUserId(fixtureInquirerId.value)).hasSize(0)
    }

    @Test
    internal fun `should not delete a token`() {
        assertThat(fcmTokenDao.findByUserId(fixtureInquirerId.value)).hasSize(1)

        repository.deleteToken(fixtureInquirerFcmToken, fixtureImam)

        assertThat(fcmTokenDao.findByUserId(fixtureInquirerId.value)).hasSize(1)
    }
}