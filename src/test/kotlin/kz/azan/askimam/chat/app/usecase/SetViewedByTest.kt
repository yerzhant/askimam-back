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

internal class SetViewedByTest : ChatFixtures() {

    @Test
    internal fun `should set viewed by an inquirer`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()

        assertThat(SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId1).isEmpty).isTrue

        verify { chatRepository.update(any()) }
    }

    @Test
    internal fun `should not set viewed by an inquirer - update error`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()
        every { chatRepository.update(any()) } returns some(Declination.withReason("x"))

        assertThat(SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId1).isDefined).isTrue
    }

    @Test
    internal fun `should set viewed by an imam`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()

        assertThat(SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId1).isEmpty).isTrue

        verify { chatRepository.update(any()) }
    }

    @Test
    internal fun `should not set viewed by an inquirer - the chat is not yours`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureAnotherInquirer
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())

        assertThat(SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId1).isDefined).isTrue
    }

    @Test
    internal fun `should not set the flag - id not found`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns left(Declination.withReason("id not found"))

        assertThat(
            SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId1)
        ).isEqualTo(some(Declination.withReason("id not found")))
    }

    private fun fixtures() {
        fixtureClock()
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()
    }
}
