package kz.azan.askimam.user.infra

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException
import java.util.*

internal class UserJdbcRepositoryTest : ChatFixtures() {

    private val dao = mockk<UserDao>()

    private val fcmTokenDao = mockk<FcmTokenDao>()

    private val repository = UserJdbcRepository(dao, fcmTokenDao)

    @Test
    internal fun `should return an imam user`() {
        fixture()

        val user = repository.findById(fixtureImamId).get()

        with(user) {
            assertThat(name).isEqualTo(NonBlankString.of("Jon Dow"))
            assertThat(type).isEqualTo(fixtureImam.type)
            assertThat(fcmTokens).isEqualTo(setOf(fixtureImamFcmToken))
        }

        verify { dao.findById(fixtureImamId.value.toInt()) }
    }

    @Test
    internal fun `should return an inquirer user`() {
        fixture()

        val user = repository.findById(fixtureInquirerId).get()

        with(user) {
            assertThat(name).isEqualTo(NonBlankString.of("Some Body"))
            assertThat(type).isEqualTo(fixtureInquirer.type)
            assertThat(fcmTokens).isEqualTo(setOf(fixtureInquirerFcmToken))
        }

        verify { dao.findById(fixtureInquirerId.value.toInt()) }
    }

    @Test
    internal fun `should not return any user`() {
        every { dao.findById(2) } returns Optional.empty()

        assertThat(repository.findById(fixtureInquirerId).left)
            .isEqualTo(Declination.withReason("The user is not found"))
    }

    @Test
    internal fun `should not return any user - db exception`() {
        every { dao.findById(2) } throws Exception("x")

        assertThat(repository.findById(fixtureInquirerId).left)
            .isEqualTo(Declination.withReason("x"))
    }

    @Test
    internal fun `should not return any user - db exception in fcm dao`() {
        fixture()
        every { fcmTokenDao.findByUserId(2) } throws Exception("x")

        assertThat(repository.findById(fixtureInquirerId).left)
            .isEqualTo(Declination.withReason("x"))
    }

    @Test
    internal fun `should find by username and status`() {
        fixture()

        assertThat(repository.findByUsernameAndStatus("jon-dow", 1).isRight).isTrue

        verify { dao.findByUsernameAndStatus("jon-dow", 1) }
    }

    @Test
    internal fun `should not find by username and status`() {
        every { dao.findByUsernameAndStatus("jon-dow", 1) } returns null

        assertThat(repository.findByUsernameAndStatus("jon-dow", 1).isLeft).isTrue
    }

    @Test
    internal fun `should not find by username and status - db error`() {
        every { dao.findByUsernameAndStatus("jon-dow", 1) } throws Exception("x")

        assertThat(repository.findByUsernameAndStatus("jon-dow", 1).isLeft).isTrue
    }

    @Test
    internal fun `should not find by username and status - fcm db error`() {
        fixture()
        every { fcmTokenDao.findByUserId(1) } throws Exception("x")

        assertThat(repository.findByUsernameAndStatus("jon-dow", 1).isLeft).isTrue
    }

    @Test
    internal fun `should save tokens`() {
        val tokens = saveTokensFixture()
        every { fcmTokenDao.deleteAll(tokens) } returns Unit
        every { fcmTokenDao.saveAll(tokens) } returns tokens

        repository.saveTokens(fixtureInquirer)

        verifySequence {
            fcmTokenDao.deleteAll(tokens)
            fcmTokenDao.saveAll(tokens)
        }
    }

    @Test
    internal fun `should not save tokens`() {
        val tokens = saveTokensFixture()
        every { fcmTokenDao.deleteAll(tokens) } returns Unit
        every { fcmTokenDao.saveAll(tokens) } throws SQLException("x")

        assertThrows<SQLException> { repository.saveTokens(fixtureInquirer) }
    }

    @Test
    internal fun `should delete a token`() {
        every { fcmTokenDao.deleteByValueAndUserId(any(), any()) } returns Unit

        val deleteToken = repository.deleteToken(fixtureInquirerFcmToken, fixtureInquirer)

        assertThat(deleteToken.isEmpty).isTrue

        verify {
            fcmTokenDao.deleteByValueAndUserId(fixtureInquirerFcmToken.value.value, fixtureInquirerId.value)
        }
    }

    @Test
    internal fun `should not delete a token`() {
        every { fcmTokenDao.deleteByValueAndUserId(any(), any()) } throws Exception("x")

        val deleteToken = repository.deleteToken(fixtureInquirerFcmToken, fixtureInquirer)

        assertThat(deleteToken).isEqualTo(some(Declination.from(Exception("x"))))
    }

    private fun saveTokensFixture() = setOf(FcmTokenRow(fixtureInquirerFcmToken.value.value, fixtureInquirerId.value))

    private fun fixture() {
        every { dao.findById(fixtureImamId.value.toInt()) } returns Optional.of(
            UserRow(
                id = fixtureImamId.value,
                username = "jon-dow",
                firstName = "Jon",
                lastName = "Dow",
                status = 1,
                passwordHash = "123",
                roles = setOf(AuthAssignmentRow("ask-imam", "1")),
            )
        )
        every { dao.findById(fixtureInquirerId.value.toInt()) } returns Optional.of(
            UserRow(
                id = fixtureInquirerId.value,
                username = "some-body",
                firstName = "Some",
                lastName = "Body",
                status = 1,
                passwordHash = "123",
                roles = emptySet(),
            )
        )
        every { dao.findByUsernameAndStatus("jon-dow", 1) } returns UserRow(
            id = fixtureImamId.value,
            username = "jon-dow",
            firstName = "Jon",
            lastName = "Dow",
            status = 1,
            passwordHash = "123",
            roles = setOf(AuthAssignmentRow("ask-imam", "1")),
        )

        every { fcmTokenDao.findByUserId(fixtureImamId.value) } returns setOf(
            FcmTokenRow(
                fixtureImamFcmToken.value.value,
                fixtureImamId.value,
            )
        )
        every { fcmTokenDao.findByUserId(fixtureInquirerId.value) } returns setOf(
            FcmTokenRow(
                fixtureInquirerFcmToken.value.value,
                fixtureInquirerId.value,
            )
        )
    }
}
