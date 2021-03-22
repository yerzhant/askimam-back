package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.none
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.policy.DeleteMessagePolicy
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChatPolicyTest : ChatFixtures() {

    @Test
    internal fun `should not update a subject by not an author`() {
        fixtureClock()

        fixtureChat().run {
            val option = updateSubject(Subject.from("New subject"), fixtureAnotherInquirer)

            assertThat(option.isDefined).isTrue
            assertThat(subject()).isEqualTo(fixtureSubject)
        }
    }

    @Test
    internal fun `should not set 'Is viewed by imam' by non imam`() {
        fixtureClock()
        val chat = fixtureChat()

        chat.run {
            assertThat(viewedByImam(fixtureInquirer).isDefined).isTrue
            assertThat(isViewedByImam()).isFalse
        }
    }

    @Test
    internal fun `should not set 'Is viewed by an inquirer' flag`() {
        fixtureClock()
        every { messageRepository.add(fixtureSavedMessage(fixtureMessageId2)) } returns none()
        val chat = fixtureChat(fixtureNewReply)

        chat.run {
            addTextMessage(fixtureNewReply, fixtureImam)
            assertThat(viewedByInquirer(fixtureAnotherInquirer).isDefined).isTrue
            assertThat(isViewedByInquirer()).isFalse
        }
    }

    @Test
    internal fun `should not add a new message`() {
        fixtureClockAndThen(30)

        fixtureChat().run {
            val option = addTextMessage(fixtureNewMessage, User(User.Id(10), User.Type.Inquirer))

            assertThat(option.isDefined).isTrue
        }
    }

    @Test
    internal fun `should not add a new audio`() {
        val audio = NotBlankString.of("Аудио")
        fixtureClock()
        fixtureChat(audio).run {
            val option = addAudioMessage(fixtureAudio, fixtureInquirer)

            assertThat(option.isDefined).isTrue
        }
    }

    @Test
    internal fun `should not delete a message by an inquirer`() {
        fixtureClock()

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId1, DeleteMessagePolicy.forInquirer, fixtureAnotherInquirer)

            assertThat(option.isDefined).isTrue
            assertThat(messages().size).isEqualTo(1)
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageDeleted(fixtureMessageId1))
        }
    }

    @Test
    internal fun `should not update a message`() {
        fixtureClockAndThen(10)

        with(fixtureChat()) {
            val option = updateTextMessage(
                fixtureMessageId1,
                fixtureAnotherInquirer,
                fixtureNewMessage,
                UpdateMessagePolicy.forAll
            )

            assertThat(option.isDefined).isTrue
            assertThat(messages().first().text).isEqualTo(fixtureMessage)
            assertThat(messages().first().updatedAt).isNull()
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageUpdated(fixtureMessageId1, fixtureNewMessage, timeAfter(10)))
        }
    }
}