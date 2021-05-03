package kz.azan.askimam.security.web.usecase

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.security.web.dto.LogoutDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LogoutTest : ChatFixtures() {

    private val underTheTest = Logout(getCurrentUser, userRepository)

    @Test
    internal fun `should erase a token`() {
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { userRepository.deleteToken(fixtureInquirerFcmToken, fixtureInquirer) } returns none()

        val result = underTheTest(LogoutDto(fixtureInquirerFcmToken.value.value))

        assertThat(result.isEmpty).isTrue

        verify {
            userRepository.deleteToken(fixtureInquirerFcmToken, fixtureInquirer)
        }
    }

    @Test
    internal fun `should not erase a token`() {
        every { getCurrentUser() } returns some(fixtureInquirer)
        every {
            userRepository.deleteToken(
                fixtureInquirerFcmToken,
                fixtureInquirer
            )
        } returns some(Declination.withReason("x"))

        val result = underTheTest(LogoutDto(fixtureInquirerFcmToken.value.value))

        assertThat(result).isEqualTo(some(Declination.withReason("x")))
    }

    @Test
    internal fun `should not erase a token either`() {
        every { getCurrentUser() } returns none()

        val result = underTheTest(LogoutDto(fixtureInquirerFcmToken.value.value))

        assertThat(result).isEqualTo(some(Declination.withReason("Who are you?")))
    }
}
