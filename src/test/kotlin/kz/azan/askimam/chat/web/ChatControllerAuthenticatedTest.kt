package kz.azan.askimam.chat.web

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.web.dto.CreateChatDto
import kz.azan.askimam.chat.web.dto.UpdateChatDto
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.WithPrincipal
import kz.azan.askimam.user.domain.model.User.Type.Imam
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@WithPrincipal
internal class ChatControllerAuthenticatedTest : ChatControllerTest() {

    @Test
    internal fun `should be rejected with 403 for non imams`() {
        mvc.get("$url/unanswered/0/20").andExpect { status { isForbidden() } }
        mvc.patch("$url/1/return-to-unanswered").andExpect { status { isForbidden() } }
    }

    @Test
    internal fun `should get my chats`() {
        every { getMyChats(0, 20) } returns right(listOfChatProjectionsFixture()
            .map { it.copy(isFavorite = true) }
        )

        mvc.get("$url/my/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data", hasSize<Any>(2))
            jsonPath("\$.data[0].id") { value(1) }
            jsonPath("\$.data[0].type") { value(Public.name) }
            jsonPath("\$.data[0].askedBy") { value(2) }
            jsonPath("\$.data[0].subject") { value("Subject") }
            jsonPath("\$.data[0].updatedAt") { value(timeAsString()) }
            jsonPath("\$.data[0].isFavorite") { value(true) }
            jsonPath("\$.data[0].isViewedByImam") { value(true) }
            jsonPath("\$.data[0].isViewedByInquirer") { value(true) }
            jsonPath("\$.data[0].messages", hasSize<Any>(1))
            jsonPath("\$.data[0].messages[0].id") { value(1) }
            jsonPath("\$.data[0].messages[0].type") { value("Text") }
            jsonPath("\$.data[0].messages[0].text") { value("A message") }
            jsonPath("\$.data[0].messages[0].author") { doesNotExist() }
            jsonPath("\$.data[0].messages[0].createdAt") { value(timeAsString()) }
            jsonPath("\$.data[0].messages[0].updatedAt") { doesNotExist() }
        }

        verify { getMyChats(0, 20) }
    }

    @Test
    internal fun `should not get my chats`() {
        every { getMyChats(0, 20) } returns left(Declination.withReason("x"))

        mvc.get("$url/my/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    @WithPrincipal(authority = Imam)
    internal fun `should get unanswered chats`() {
        every { getUnansweredChats(0, 20) } returns right(listOfChatProjectionsFixture())

        mvc.get("$url/unanswered/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data", hasSize<Any>(2))
            jsonPath("\$.data[0].id") { value(1) }
            jsonPath("\$.data[0].type") { value(Public.name) }
            jsonPath("\$.data[0].askedBy") { value(2) }
            jsonPath("\$.data[0].subject") { value("Subject") }
            jsonPath("\$.data[0].updatedAt") { value(timeAsString()) }
            jsonPath("\$.data[0].isViewedByImam") { value(true) }
            jsonPath("\$.data[0].isViewedByInquirer") { value(true) }
            jsonPath("\$.data[0].messages", hasSize<Any>(1))
            jsonPath("\$.data[0].messages[0].id") { value(1) }
            jsonPath("\$.data[0].messages[0].type") { value("Text") }
            jsonPath("\$.data[0].messages[0].text") { value("A message") }
            jsonPath("\$.data[0].messages[0].author") { doesNotExist() }
            jsonPath("\$.data[0].messages[0].createdAt") { value(timeAsString()) }
            jsonPath("\$.data[0].messages[0].updatedAt") { doesNotExist() }
        }

        verify { getUnansweredChats(0, 20) }
    }

    @Test
    @WithPrincipal(authority = Imam)
    internal fun `should not get unanswered chats`() {
        every { getUnansweredChats(0, 20) } returns left(Declination.withReason("x"))

        mvc.get("$url/unanswered/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    internal fun `should create a chat without a subject`() {
        every { createChat(Public, fixtureMessage, fixtureInquirerFcmToken) } returns none()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateChatDto(Public, null, "A message", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }

        verify { createChat(Public, fixtureMessage, fixtureInquirerFcmToken) }
    }

    @Test
    internal fun `should not create a chat without a subject`() {
        every { createChat(Public, fixtureMessage, fixtureInquirerFcmToken) } returns some(Declination.withReason("x"))

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateChatDto(Public, null, "A message", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    internal fun `should create a chat with a subject`() {
        every { createChat.withSubject(Public, fixtureSubject, fixtureMessage, fixtureInquirerFcmToken) } returns none()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateChatDto(Public, "Subject", "A message", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }

        verify { createChat.withSubject(Public, fixtureSubject, fixtureMessage, fixtureInquirerFcmToken) }
    }

    @Test
    internal fun `should not create a chat with a subject`() {
        every { createChat.withSubject(Public, fixtureSubject, fixtureMessage, fixtureInquirerFcmToken) } returns some(
            Declination.withReason("x")
        )

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreateChatDto(Public, "Subject", "A message", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    internal fun `should delete a chat`() {
        every { deleteChat(fixtureChatId1) } returns none()

        mvc.delete("$url/1").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }

        verify { deleteChat(fixtureChatId1) }
    }

    @Test
    internal fun `should not delete a chat`() {
        every { deleteChat(fixtureChatId1) } returns some(Declination.withReason("x"))

        mvc.delete("$url/1").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    internal fun `should update a chat's subject`() {
        every { updateChatSubject(fixtureChatId1, fixtureSubject) } returns none()

        mvc.patch("$url/1") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateChatDto("Subject"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }

        verify { updateChatSubject(fixtureChatId1, fixtureSubject) }
    }

    @Test
    internal fun `should not update a chat's subject`() {
        every { updateChatSubject(fixtureChatId1, fixtureSubject) } returns some(Declination.withReason("x"))

        mvc.patch("$url/1") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateChatDto("Subject"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    internal fun `should set Viewed by flag`() {
        every { setViewedBy(fixtureChatId1) } returns none()

        mvc.patch("$url/1/viewed-by").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }

        verify { setViewedBy(fixtureChatId1) }
    }

    @Test
    internal fun `should not set Viewed by flag`() {
        every { setViewedBy(fixtureChatId1) } returns some(Declination.withReason("x"))

        mvc.patch("$url/1/viewed-by").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    @WithPrincipal(authority = Imam)
    internal fun `should return to unanswered list`() {
        every { returnChatToUnansweredList(fixtureChatId1) } returns none()

        mvc.patch("$url/1/return-to-unanswered").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }

        verify { returnChatToUnansweredList(fixtureChatId1) }
    }

    @Test
    @WithPrincipal(authority = Imam)
    internal fun `should not return to unanswered list`() {
        every { returnChatToUnansweredList(fixtureChatId1) } returns some(Declination.withReason("x"))

        mvc.patch("$url/1/return-to-unanswered").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }
}