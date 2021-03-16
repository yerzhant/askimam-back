package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NotBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ChatTest {
    @Test
    internal fun `should create a chat`() {
        val subject = NotBlankString.of("Subject")
        val firstMessage = NotBlankString.of("A message")

        val chat = Chat(subject, firstMessage)

        with(chat) {
            assertThat(subject()).isEqualTo(NotBlankString.of("Subject"))
            assertThat(messages().size).isEqualTo(1)
            assertThat(messages().first().text).isEqualTo(NotBlankString.of("A message"))
        }
    }
}