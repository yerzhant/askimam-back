package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChatPolicyTest : ChatFixtures() {

    @Test
    internal fun `should not update a subject by not an author`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            some(fixtureInquirer),
            some(fixtureAnotherInquirer)
        )

        fixtureChat().run {
            val option = updateSubject(Subject.from("New subject"))

            assertThat(option.isDefined).isTrue
            assertThat(subject()).isEqualTo(fixtureSubject)
        }
    }

    @Test
    internal fun `should not set 'Is viewed by imam' by non imam`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        val chat = fixtureChat()

        chat.run {
            assertThat(viewedByImam().isDefined).isTrue
            assertThat(isViewedByImam()).isFalse
        }
    }

    @Test
    internal fun `should not set 'Is viewed by an inquirer' flag`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            some(fixtureInquirer),
            some(fixtureImam),
            some(fixtureAnotherInquirer)
        )
        val chat = fixtureChat(fixtureNewReply)

        chat.run {
            addTextMessage(fixtureNewReply, fixtureInquirerFcmToken)
            assertThat(viewedByInquirer().isDefined).isTrue
            assertThat(isViewedByInquirer()).isFalse
        }
    }

    @Test
    internal fun `should not add a new message`() {
        fixtureClockAndThen(30)
        every { getCurrentUser() } returnsMany listOf(
            some(fixtureInquirer),
            some(fixtureAnotherInquirer),
        )

        fixtureChat().run {
            val option = addTextMessage(fixtureNewMessage, fixtureInquirerFcmToken)

            assertThat(option.isDefined).isTrue
        }
    }

    @Test
    internal fun `should not add a new audio`() {
        val audio = NonBlankString.of("Аудио")
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)

        fixtureChat(audio).run {
            val option = addAudioMessage(fixtureAudio, fixtureAudioDuration, fixtureImamFcmToken)

            assertThat(option.isDefined).isTrue
        }
    }

    @Test
    internal fun `should not delete a message by an inquirer`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureAnotherInquirer)

        with(fixtureSavedChat()) {
            val option = deleteMessage(fixtureMessageId1)

            assertThat(option.isDefined).isTrue
            assertThat(messages().size).isEqualTo(3)
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageDeleted(fixtureMessageId1, null))
        }
    }

    @Test
    internal fun `should not update a message`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureAnotherInquirer)

        with(fixtureSavedChat()) {
            val option = updateTextMessage(fixtureMessageId1, fixtureNewMessage, fixtureInquirerFcmToken)

            assertThat(option.isDefined).isTrue
            assertThat(messages().first().text()).isEqualTo(fixtureMessage)
            assertThat(messages().first().updatedAt()).isNull()
        }

        verify(exactly = 0) {
            eventPublisher.publish(any())
        }
    }

    @Test
    internal fun `should not update an audio message`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureImam)

        with(fixtureSavedChat()) {
            val option = updateTextMessage(fixtureMessageId3, fixtureNewReply, fixtureImamFcmToken)

            assertThat(option).isEqualTo(some(Declination.withReason("An audio message may not be edited")))
            assertThat(messages().first().updatedAt()).isNull()
        }
    }

    @Test
    internal fun `should not return a chat by a not imam to not answered`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)

        with(fixtureSavedChat()) {
            assertThat(returnToUnansweredList().isDefined).isTrue
            assertThat(answeredBy()).isNotNull
            assertThat(imamFcmToken()).isNotNull
        }
    }
}