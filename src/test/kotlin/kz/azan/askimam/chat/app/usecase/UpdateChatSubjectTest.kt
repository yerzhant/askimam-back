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

internal class UpdateChatSubjectTest : ChatFixtures() {

    @Test
    internal fun `should update a chat's subject`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()

        assertThat(UpdateChatSubject(chatRepository)(fixtureChatId1, fixtureSubject).isEmpty).isTrue

        verify { chatRepository.update(any()) }
    }

    @Test
    internal fun `should not update a chat's - id not found`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns left(Declination.withReason("id not found"))

        assertThat(
            UpdateChatSubject(chatRepository)(fixtureChatId1, fixtureSubject)
        ).isEqualTo(some(Declination.withReason("id not found")))
    }

    @Test
    internal fun `should not update a chat's - update failed`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns some(Declination.withReason("x"))

        assertThat(
            UpdateChatSubject(chatRepository)(fixtureChatId1, fixtureSubject)
        ).isEqualTo(some(Declination.withReason("x")))
    }
}