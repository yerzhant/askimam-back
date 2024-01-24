package kz.azan.askimam.user.app.usecase

import io.mockk.every
import io.mockk.verifySequence
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UpdateFcmTokenTest : ChatFixtures() {

    private lateinit var useCase: UpdateFcmToken

    @BeforeEach
    fun setUp() {
        useCase = UpdateFcmToken(getCurrentUser, userRepository)
    }

    @Test
    fun `unknown user`() {
        every { getCurrentUser() } returns none()

        val result = useCase.process(FcmToken.from("123"), FcmToken.from("456"))

        assertThat(result.isDefined).isTrue()
        assertThat(result.get()).isEqualTo(Declination.withReason("Who are you?"))
    }

    @Test
    fun `update an old token by a new one`() {
        val oldToken = FcmToken.from("123")
        val newToken = FcmToken.from("456")
        val user = fixtureInquirer.apply {
            fcmTokens.clear()
            fcmTokens.add(oldToken)
        }

        every { getCurrentUser() } returns some(user)
        every { userRepository.saveTokens(any()) } returns Unit

        val result = useCase.process(oldToken, newToken)

        assertThat(result.isEmpty).isTrue()
        assertThat(user.fcmTokens.size).isEqualTo(1)
        assertThat(user.fcmTokens.contains(oldToken)).isFalse()
        assertThat(user.fcmTokens.contains(newToken)).isTrue()

        verifySequence {
            userRepository.saveTokens(fixtureInquirer)
        }
    }

    @Test
    fun `add a new one`() {
        val oldToken = FcmToken.from("123")
        val newToken = FcmToken.from("456")
        val user = fixtureInquirer.apply {
            fcmTokens.clear()
        }

        every { getCurrentUser() } returns some(user)
        every { userRepository.saveTokens(any()) } returns Unit

        val result = useCase.process(oldToken, newToken)

        assertThat(result.isEmpty).isTrue()
        assertThat(user.fcmTokens.size).isEqualTo(1)
        assertThat(user.fcmTokens.contains(oldToken)).isFalse()
        assertThat(user.fcmTokens.contains(newToken)).isTrue()

        verifySequence {
            userRepository.saveTokens(fixtureInquirer)
        }
    }
}