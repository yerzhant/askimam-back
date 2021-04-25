package kz.azan.askimam.chat.web

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.common.domain.Declination
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

internal class ChatControllerPublicTest : ChatControllerTest() {

    @Test
    internal fun `should be rejected with 401`() {
        mvc.get("$url/my/0/20").andExpect { status { isUnauthorized() } }
        mvc.get("$url/unanswered/0/20").andExpect { status { isUnauthorized() } }
        mvc.post(url).andExpect { status { isUnauthorized() } }
        mvc.delete("$url/1").andExpect { status { isUnauthorized() } }
        mvc.patch("$url/1").andExpect { status { isUnauthorized() } }
        mvc.patch("$url/1/viewed-by").andExpect { status { isUnauthorized() } }
        mvc.patch("$url/1/return-to-unanswered").andExpect { status { isUnauthorized() } }
    }

    @Test
    internal fun `should get public chats`() {
        every { getPublicChats(0, 20) } returns right(listOfChatProjectionsFixture())

        mvc.get("$url/public/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data", hasSize<Any>(2))
            jsonPath("\$.data[0].id") { value(1) }
            jsonPath("\$.data[0].askedBy") { value(2) }
            jsonPath("\$.data[0].subject") { value("Subject") }
            jsonPath("\$.data[0].isFavorite") { value(false) }
        }

        verify { getPublicChats(0, 20) }
    }

    @Test
    internal fun `should not get public chats`() {
        every { getPublicChats(0, 20) } returns left(Declination.withReason("x"))

        mvc.get("$url/public/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    internal fun `should get a chat`() {
        fixtureClock()
        every { userRepository.findById(fixtureImamId) } returns right(fixtureImam)
        every { getChatUseCase(fixtureChatId1) } returns ChatProjection.from(fixtureSavedChat(), userRepository)

        mvc.get("$url/messages/1").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data.id") { value(1) }
            jsonPath("\$.data.askedBy") { value(2) }
            jsonPath("\$.data.subject") { value("Subject") }
            jsonPath("\$.data.messages", hasSize<Any>(3))
            jsonPath("\$.data.messages[0].id") { value(1) }
            jsonPath("\$.data.messages[0].type") { value("Text") }
            jsonPath("\$.data.messages[0].text") { value("A message") }
            jsonPath("\$.data.messages[0].author") { doesNotExist() }
            jsonPath("\$.data.messages[0].createdAt") { `is`(timeAfter(0)) }
            jsonPath("\$.data.messages[0].updatedAt") { doesNotExist() }
            jsonPath("\$.data.messages[1].id") { value(2) }
            jsonPath("\$.data.messages[1].type") { value("Text") }
            jsonPath("\$.data.messages[1].text") { value("A message") }
            jsonPath("\$.data.messages[1].author") { value("Imam") }
            jsonPath("\$.data.messages[1].createdAt") { `is`(timeAfter(0)) }
            jsonPath("\$.data.messages[1].updatedAt") { doesNotExist() }
        }

        verify { getChatUseCase(fixtureChatId1) }
    }

    @Test
    internal fun `should not get a chat`() {
        every { getChatUseCase(fixtureChatId1) } returns left(Declination.withReason("x"))

        mvc.get("$url/messages/1").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }
}