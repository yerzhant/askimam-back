package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetUnansweredChatsTest : ChatFixtures() {

    @Test
    internal fun `should get the list`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findUnansweredChats(0, 20) } returns right(fixtureSavedTwoChats())

        val list = GetUnansweredChats(chatRepository, getCurrentUser)(0, 20).get()

        assertThat(list).hasSize(2)
    }

    @Test
    internal fun `should not get the list - find error`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findUnansweredChats(0, 20) } returns left(Declination.withReason("x"))

        val list = GetUnansweredChats(chatRepository, getCurrentUser)(0, 20)

        assertThat(list.isLeft).isTrue
    }

    @Test
    internal fun `should not get the list - not an imam`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer

        val list = GetUnansweredChats(chatRepository, getCurrentUser)(0, 20)

        assertThat(list.isLeft).isTrue
    }
}