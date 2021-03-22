package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.model.ChatFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AddAudioMessageTest : ChatFixtures() {

    @Test
    internal fun `should add a text message`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()

        assertThat(AddAudioMessage(chatRepository)(fixtureChatId, fixtureAudio).isEmpty).isTrue
    }

    @Test
    internal fun `should not add a text message`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()

        assertThat(AddAudioMessage(chatRepository)(fixtureChatId, fixtureAudio).isDefined).isTrue
    }

    private fun fixtures() {
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { messageRepository.generateId() } returns right(fixtureMessageId1)
        every { chatRepository.update(any()) } returns none()
        every { messageRepository.add(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
    }
}