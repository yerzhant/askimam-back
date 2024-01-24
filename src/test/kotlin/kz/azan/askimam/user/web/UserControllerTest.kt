package kz.azan.askimam.user.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import kz.azan.askimam.user.app.usecase.UpdateFcmToken
import kz.azan.askimam.user.web.dto.UpdateFcmTokenDto
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.patch

@WebMvcTest(UserController::class)
class UserControllerTest : ControllerTest() {

    private val url = "/user"

    @MockkBean
    private lateinit var updateFcmToken: UpdateFcmToken

    @Test
    fun `update token - unauthorized`() {
        mvc.patch("$url/update-token") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateFcmTokenDto("123", "456"))
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithPrincipal
    fun `update token - error`() {
        every { updateFcmToken.process(any(), any()) } returns some(Declination.withReason("err"))

        mvc.patch("$url/update-token") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateFcmTokenDto("123", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("err") }
        }
    }

    @Test
    @WithPrincipal
    fun `update token - ok`() {
        every { updateFcmToken.process(any(), any()) } returns none()

        mvc.patch("$url/update-token") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateFcmTokenDto("123", "456"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }
    }
}