package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SetViewedByTest : ChatFixtures() {

    @Test
    internal fun `should set viewed by an inquirer`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()

        assertThat(SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId).isEmpty).isTrue
    }

    @Test
    internal fun `should set viewed by an imam`() {
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()

        assertThat(SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId).isEmpty).isTrue
    }

    @Test
    internal fun `should not set viewed by an inquirer - the chat is not yours`() {
        every { getCurrentUser() } returns fixtureAnotherInquirer
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())

        assertThat(SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId).isDefined).isTrue
    }

    @Test
    internal fun `should not set the flag - id not found`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns left(Declination.withReason("id not found"))

        assertThat(
            SetViewedBy(getCurrentUser, chatRepository)(fixtureChatId)
        ).isEqualTo(some(Declination.withReason("id not found")))
    }
}