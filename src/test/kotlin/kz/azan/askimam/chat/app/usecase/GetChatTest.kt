package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetChatTest : ChatFixtures() {

    @Test
    internal fun `should return a chat`() {
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(Chat.Id(1)) } returns right(chat)

        assertThat(GetChat(getCurrentUser, chatRepository)(Chat.Id(1)).get()).isEqualTo(chat)
    }

    @Test
    internal fun `should return a chat to an imam`() {
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findById(Chat.Id(1)) } returns right(chat)

        assertThat(GetChat(getCurrentUser, chatRepository)(Chat.Id(1)).get()).isEqualTo(chat)
    }

    @Test
    internal fun `chat is not found`() {
        val declination = Declination.withReason("Chat is not found")
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findById(Chat.Id(1)) } returns left(declination)

        assertThat(GetChat(getCurrentUser, chatRepository)(Chat.Id(1)).left).isEqualTo(declination)
    }

    @Test
    internal fun `should return a publicly visible chat`() {
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns fixtureAnotherInquirer
        every { chatRepository.findById(Chat.Id(1)) } returns right(chat)

        assertThat(GetChat(getCurrentUser, chatRepository)(Chat.Id(1)).get()).isEqualTo(chat)
    }

    @Test
    internal fun `should deny access to a private chat`() {
        val chat = fixtureSavedChat(Private)
        val declination = Declination.withReason("The operation is not permitted")
        every { getCurrentUser() } returns fixtureAnotherInquirer
        every { chatRepository.findById(Chat.Id(1)) } returns right(chat)

        assertThat(GetChat(getCurrentUser, chatRepository)(Chat.Id(1)).left).isEqualTo(declination)
    }
}