package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.policy.AddMessagePolicy
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChatPolicyTest : ChatFixtures() {

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
}