package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetMyChatsTest : ChatFixtures() {

    @Test
    internal fun `should get my chats`() {
        fixtureClock()
        every { chatRepository.findMyChats(0, 20) } returns right(fixtureSavedTwoChats().reversed())

        val list = GetMyChats(chatRepository)(0, 20).get()

        assertThat(list).hasSize(2)
    }

    @Test
    internal fun `should not get my chats`() {
        fixtureClock()
        every { chatRepository.findMyChats(0, 20) } returns left(Declination.withReason("x"))

        assertThat(GetMyChats(chatRepository)(0, 20).isLeft).isTrue
    }
}