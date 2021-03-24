package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.verifySequence
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AddAudioMessageTest : ChatFixtures() {

    @Test
    internal fun `should add an audio message`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()

        assertThat(AddAudioMessage(chatRepository)(fixtureChatId, fixtureAudio, fixtureImamFcmToken).isEmpty).isTrue

        verifySequence {
            chatRepository.findById(any())
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureAudioText))
            chatRepository.update(any())
        }
    }

    @Test
    internal fun `should not add an audio message - id not found`() {
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findById(any()) } returns left(Declination.withReason("error"))

        assertThat(AddAudioMessage(chatRepository)(fixtureChatId, fixtureAudio, fixtureImamFcmToken).get()).isEqualTo(
            Declination.withReason("error")
        )
    }

    @Test
    internal fun `should not add an audio message - update error`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()
        every { chatRepository.update(any()) } returns some(Declination.withReason("error"))

        assertThat(AddAudioMessage(chatRepository)(fixtureChatId, fixtureAudio, fixtureImamFcmToken).get()).isEqualTo(
            Declination.withReason("error")
        )
    }

    @Test
    internal fun `should not add an audio message`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()

        assertThat(AddAudioMessage(chatRepository)(fixtureChatId, fixtureAudio, fixtureImamFcmToken).isDefined).isTrue
    }

    private fun fixtures() {
        fixtureClock()
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
    }
}