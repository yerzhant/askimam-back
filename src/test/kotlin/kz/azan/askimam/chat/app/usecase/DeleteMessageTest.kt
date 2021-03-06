package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DeleteMessageTest : ChatFixtures() {

    @Test
    internal fun `should delete a message`() {
        every { getCurrentUser() } returns some(fixtureInquirer)
        fixtures()

        assertThat(DeleteMessage(chatRepository)(fixtureChatId1, fixtureMessageId1).isEmpty).isTrue

        verify {
            chatRepository.findById(any())
            chatRepository.update(any())
        }
    }

    @Test
    internal fun `should not delete a message - chat not found`() {
        every { chatRepository.findById(any()) } returns left(Declination.withReason("x"))

        assertThat(DeleteMessage(chatRepository)(fixtureChatId1, fixtureMessageId1).isDefined).isTrue
    }

    @Test
    internal fun `should not delete a message - update error`() {
        fixtures()
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { chatRepository.update(any()) } returns some(Declination.withReason("x"))

        assertThat(DeleteMessage(chatRepository)(fixtureChatId1, fixtureMessageId1).isDefined).isTrue
    }

    @Test
    internal fun `should delete a message by an imam`() {
        every { getCurrentUser() } returns some(fixtureImam)
        fixtures()

        assertThat(DeleteMessage(chatRepository)(fixtureChatId1, fixtureMessageId1).isEmpty).isTrue
    }

    @Test
    internal fun `should not delete a message`() {
        every { getCurrentUser() } returns some(fixtureAnotherInquirer)
        fixtures()

        assertThat(DeleteMessage(chatRepository)(fixtureChatId1, fixtureMessageId1).isDefined).isTrue
    }

    private fun fixtures() {
        fixtureClock()
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
    }
}