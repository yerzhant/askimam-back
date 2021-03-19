package kz.azan.askimam.chat.domain.model

import org.junit.jupiter.api.Test

class ChatPolicyTest : ChatFixtures() {

    @Test
    internal fun `should add a new message`() {
        fixtureClockAndThen(30)

        fixturePublicChat().run {
            addTextMessageByInquirer(fixtureMessageId, fixtureNewMessage)

        }
    }

}