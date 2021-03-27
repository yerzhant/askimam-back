package kz.azan.askimam.user.infra

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class UserJdbcRepositoryTest : ChatFixtures() {

    private val dao = mockk<UserDao>()

    private val repository = UserJdbcRepository(dao)

    @Test
    internal fun `should return an imam user`() {
        every { dao.findById(1) } returns Optional.of(
            UserRow(
                id = 1,
                username = "jon-dow",
                firstName = "Jon",
                lastName = "Dow",
                status = 1,
                passwordHash = "123",
                roles = setOf(AuthAssignmentRow("ask-imam", "1")),
            )
        )

        val user = repository.findById(fixtureImamId).get()

        with(user) {
            assertThat(name).isEqualTo(NonBlankString.of("Jon Dow"))
            assertThat(type).isEqualTo(fixtureImam.type)
        }

        verify { dao.findById(fixtureImamId.value.toInt()) }
    }

    @Test
    internal fun `should return an inquirer user`() {
        every { dao.findById(2) } returns Optional.of(
            UserRow(
                id = 1,
                username = "some-body",
                firstName = "Some",
                lastName = "Body",
                status = 1,
                passwordHash = "123",
                roles = emptySet(),
            )
        )

        val user = repository.findById(fixtureInquirerId).get()

        with(user) {
            assertThat(name).isEqualTo(NonBlankString.of("Some Body"))
            assertThat(type).isEqualTo(fixtureInquirer.type)
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
    internal fun `should find by username and status`() {
        every { dao.findByUsernameAndStatus("jon-dow", 1) } returns UserRow(
            id = 1,
            username = "jon-dow",
            firstName = "Jon",
            lastName = "Dow",
            status = 1,
            passwordHash = "123",
            roles = setOf(AuthAssignmentRow("ask-imam", "1")),
        )

        assertThat(repository.findByUsernameAndStatus("jon-dow", 1).isRight).isTrue

        verify { dao.findByUsernameAndStatus("jon-dow", 1) }
    }

    @Test
    internal fun `should not find by username and status`() {
        every { dao.findByUsernameAndStatus("jon-dow", 1) } returns null

        assertThat(repository.findByUsernameAndStatus("jon-dow", 1).isLeft).isTrue
    }
}