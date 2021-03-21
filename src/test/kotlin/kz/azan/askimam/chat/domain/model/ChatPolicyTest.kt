package kz.azan.askimam.chat.domain.model

import io.mockk.verify
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy
import kz.azan.askimam.chat.domain.policy.DeleteMessagePolicy
import kz.azan.askimam.chat.domain.policy.UpdateChatPolicy
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChatPolicyTest : ChatFixtures() {


    @Test
    internal fun `should not rename a subject by not an author`() {
        fixtureClock()

        fixtureChat().run {
            val option = renameSubject(
                NotBlankString.of("New subject"),
                UpdateChatPolicy.forInquirer,
                fixtureAnotherInquirer
            )

            assertThat(option.isDefined).isTrue
            assertThat(subject()).isEqualTo(fixtureSubject)
        }
    }

    @Test
    internal fun `should not rename a subject by not an imam`() {
        fixtureClock()

        fixtureChat().run {
            val option = renameSubject(NotBlankString.of("New subject"), UpdateChatPolicy.forImam, fixtureInquirer)

            assertThat(option.isDefined).isTrue
            assertThat(subject()).isEqualTo(fixtureSubject)
        }
    }

    @Test
    internal fun `should not add a new message`() {
        fixtureClockAndThen(30)

        fixtureChat().run {
            val option = addTextMessage(
                AddMessagePolicy.forInquirer,
                fixtureMessageId,
                fixtureNewMessage,
                User(User.Id(10), User.Type.Inquirer),
            )

            assertThat(option.isDefined).isTrue
        }
    }

    @Test
    internal fun `should not add a new audio`() {
        val audio = NotBlankString.of("Аудио")
        fixtureClock()
        fixtureChat(audio).run {
            val option = addAudioMessage(AddMessagePolicy.forImam, Message.Id(2), fixtureAudio, fixtureInquirer)

            assertThat(option.isDefined).isTrue
        }
    }

    @Test
    internal fun `should not delete a message by an inquirer`() {
        fixtureClock()

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId, DeleteMessagePolicy.forInquirer, fixtureAnotherInquirer)

            assertThat(option.isDefined).isTrue
            assertThat(messages().size).isEqualTo(1)
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageDeleted(fixtureMessageId))
        }
    }

    @Test
    internal fun `should not update a message`() {
        fixtureClockAndThen(10)

        with(fixtureChat()) {
            val option = updateTextMessage(
                fixtureMessageId,
                fixtureAnotherInquirer,
                fixtureNewMessage,
                UpdateMessagePolicy.forAll
            )

            assertThat(option.isDefined).isTrue
            assertThat(messages().first().text).isEqualTo(fixtureMessage)
            assertThat(messages().first().updatedAt).isNull()
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageUpdated(fixtureMessageId, fixtureNewMessage, timeAfter(10)))
        }
    }
}