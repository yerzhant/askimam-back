package kz.azan.askimam.chat.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.app.usecase.AddAudioMessage
import kz.azan.askimam.chat.app.usecase.AddTextMessage
import kz.azan.askimam.chat.web.dto.AddAudioMessageDto
import kz.azan.askimam.chat.web.dto.AddTextMessageDto
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import kz.azan.askimam.user.domain.model.User.Type.Imam
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@WithPrincipal
@WebMvcTest(MessageController::class)
internal class MessageControllerTest : ControllerTest() {

    private val url = "/messages"

    @MockkBean
    private lateinit var addTextMessage: AddTextMessage

    @MockkBean
    private lateinit var addAudioMessage: AddAudioMessage

    @Test
    @WithAnonymousUser
    internal fun `should be rejected with 401`() {
        mvc.post(url).andExpect { status { isUnauthorized() } }
        mvc.post("$url/audio").andExpect { status { isUnauthorized() } }
        mvc.delete("$url/1").andExpect { status { isUnauthorized() } }
        mvc.patch("$url/1").andExpect { status { isUnauthorized() } }

        // delete message
        // update text message
    }

    @Test
    internal fun `should be rejected with 403 for non imams`() {
        mvc.post("$url/audio").andExpect { status { isForbidden() } }
    }

    @Test
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

    @Test
    @WithPrincipal(authority = Imam)
    internal fun `should add an audio message`() {
        every { addAudioMessage(fixtureChatId1, fixtureAudio, fixtureImamFcmToken) } returns none()

        mvc.post("$url/audio") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AddAudioMessageDto(1, "audio.mp3", "123"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }
    }

    @Test
    @WithPrincipal(authority = Imam)
    internal fun `should not add an audio message`() {
        every {
            addAudioMessage(fixtureChatId1, fixtureAudio, fixtureImamFcmToken)
        } returns some(Declination.withReason("x"))

        mvc.post("$url/audio") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AddAudioMessageDto(1, "audio.mp3", "123"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }
}