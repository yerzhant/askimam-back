package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.common.domain.Notifications
import kz.azan.askimam.common.type.NotBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ChatTest {
    private val notifications = mockk<Notifications>()

    @Test
    internal fun `should create a chat`() {
        val subject = NotBlankString.of("Subject")
        val firstMessage = NotBlankString.of("A message")

        every { notifications.notify(ChatCreated(subject, firstMessage)) } returns Unit

        val chat = Chat(notifications, subject, firstMessage)

        with(chat) {
            assertThat(subject()).isEqualTo(NotBlankString.of("Subject"))
            assertThat(messages().size).isEqualTo(1)
            assertThat(messages().first().text).isEqualTo(NotBlankString.of("A message"))
        }

        verifySequence { notifications.notify(ChatCreated(subject, firstMessage)) }
    }
}