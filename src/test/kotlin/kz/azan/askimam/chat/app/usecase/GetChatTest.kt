package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetChatTest : ChatFixtures() {

    @Test
    internal fun `should return a chat`() {
        val chatProjection = fixtures()
        every { getCurrentUser() } returns fixtureInquirer

        val result = underTest().get()

        assertThat(result).isEqualTo(chatProjection)
        assertThat(result.id).isEqualTo(fixtureChatId1)
        assertThat(result.subject).isEqualTo(fixtureSubject)
        assertThat(result.messages).hasSize(3)
        assertThat(result.messages?.first()?.id).isEqualTo(fixtureMessageId1)
        assertThat(result.messages?.first()?.type).isEqualTo(Text)
        assertThat(result.messages?.first()?.text).isEqualTo(fixtureMessage)
        assertThat(result.messages?.first()?.author).isEqualTo(fixtureInquirer)
        assertThat(result.messages?.first()?.createdAt).isEqualTo(timeAfter(0))
        assertThat(result.messages?.first()?.updatedAt).isNull()
    }

    @Test
    internal fun `should return a chat to an imam`() {
        val chatProjection = fixtures()
        every { getCurrentUser() } returns fixtureImam

        assertThat(underTest().get()).isEqualTo(chatProjection)
    }

    @Test
    internal fun `chat is not found`() {
        val declination = Declination.withReason("Chat is not found")
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findById(fixtureChatId1) } returns left(declination)

        assertThat(underTest().left).isEqualTo(declination)
    }

    @Test
    internal fun `should return a publicly visible chat`() {
        val chatProjection = fixtures()
        every { getCurrentUser() } returns fixtureAnotherInquirer

        assertThat(underTest().get()).isEqualTo(chatProjection)
    }

    @Test
    internal fun `should deny access to a private chat`() {
        fixtureClock()
        val chat = fixtureSavedChat(Private)
        val declination = Declination.withReason("The operation is not permitted")
        every { getCurrentUser() } returns fixtureAnotherInquirer
        every { chatRepository.findById(fixtureChatId1) } returns right(chat)

        assertThat(underTest().left).isEqualTo(declination)
    }

    private fun underTest() = GetChat(getCurrentUser, chatRepository, userRepository)(fixtureChatId1)

    private fun fixtures(): ChatProjection {
        fixtureClock()
        val chat = fixtureSavedChat()
        every { chatRepository.findById(fixtureChatId1) } returns right(chat)
        every { userRepository.findById(fixtureInquirerId) } returns fixtureInquirer
        every { userRepository.findById(fixtureImamId) } returns fixtureImam
        return ChatProjection.from(chat, userRepository)
    }
}