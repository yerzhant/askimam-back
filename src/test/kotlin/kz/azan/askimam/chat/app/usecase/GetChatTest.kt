package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetChatTest : ChatFixtures() {

    @Test
    internal fun `should return a chat`() {
        val chat = fixtures()
        every { getCurrentUser() } returns fixtureInquirer

        assertThat(GetChat(getCurrentUser, chatRepository)(fixtureChatId1).get())
            .isEqualTo(ChatProjection.from(chat))
    }

    @Test
    internal fun `should return a chat to an imam`() {
        val chat = fixtures()
        every { getCurrentUser() } returns fixtureImam

        assertThat(GetChat(getCurrentUser, chatRepository)(fixtureChatId1).get())
            .isEqualTo(ChatProjection.from(chat))
    }

    @Test
    internal fun `chat is not found`() {
        val declination = Declination.withReason("Chat is not found")
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findById(fixtureChatId1) } returns left(declination)

        assertThat(GetChat(getCurrentUser, chatRepository)(fixtureChatId1).left).isEqualTo(declination)
    }

    @Test
    internal fun `should return a publicly visible chat`() {
        val chat = fixtures()
        every { getCurrentUser() } returns fixtureAnotherInquirer

        assertThat(GetChat(getCurrentUser, chatRepository)(fixtureChatId1).get())
            .isEqualTo(ChatProjection.from(chat))
    }

    @Test
    internal fun `should deny access to a private chat`() {
        fixtureClock()
        val chat = fixtureSavedChat(Private)
        val declination = Declination.withReason("The operation is not permitted")
        every { getCurrentUser() } returns fixtureAnotherInquirer
        every { chatRepository.findById(fixtureChatId1) } returns right(chat)

        assertThat(GetChat(getCurrentUser, chatRepository)(fixtureChatId1).left).isEqualTo(declination)
    }

    private fun fixtures(): Chat {
        fixtureClock()
        val chat = fixtureSavedChat()
        every { chatRepository.findById(fixtureChatId1) } returns right(chat)
        return chat
    }
}
