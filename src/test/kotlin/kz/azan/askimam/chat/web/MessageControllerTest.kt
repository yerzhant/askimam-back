package kz.azan.askimam.chat.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.app.usecase.AddTextMessage
import kz.azan.askimam.chat.web.dto.AddTextMessageDto
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@WebMvcTest(MessageController::class)
internal class MessageControllerTest : ControllerTest() {

    private val url = "/messages"

    @MockkBean
    private lateinit var addTextMessage: AddTextMessage

    @Test
    internal fun `should be rejected with 401`() {
        mvc.post(url).andExpect { status { isUnauthorized() } }
        mvc.delete("$url/1").andExpect { status { isUnauthorized() } }
        mvc.patch("$url/1").andExpect { status { isUnauthorized() } }

        // delete message
        // update text message
    }

    @Test
    internal fun `should be rejected with 403 for non imams`() {

        // add audio
    }

    @Test
    @WithPrincipal
    internal fun `should add a text message`() {
        every { addTextMessage(fixtureChatId1, fixtureMessage, fixtureInquirerFcmToken) } returns none()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AddTextMessageDto(1, "A message", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }
    }

    @Test
    @WithPrincipal
    internal fun `should not add a text message`() {
        every {
            addTextMessage(fixtureChatId1, fixtureMessage, fixtureInquirerFcmToken)
        } returns some(Declination.withReason("x"))

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AddTextMessageDto(1, "A message", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }
}