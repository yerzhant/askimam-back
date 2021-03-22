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

internal class UpdateChatSubjectTest : ChatFixtures() {
    @Test
    internal fun `should update a chat's subject`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()

        assertThat(UpdateChatSubject(chatRepository)(fixtureChatId, fixtureSubject).isEmpty).isTrue
    }

    @Test
    internal fun `should not update a chat's - id not found`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns left(Declination.withReason("id not found"))

        assertThat(
            UpdateChatSubject(chatRepository)(
                fixtureChatId,
                fixtureSubject
            )
        ).isEqualTo(some(Declination.withReason("id not found")))
    }
}