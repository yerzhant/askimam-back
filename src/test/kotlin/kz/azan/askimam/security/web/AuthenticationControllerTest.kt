package kz.azan.askimam.security.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.control.Either.right
import io.vavr.kotlin.left
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.security.service.UserService
import kz.azan.askimam.security.web.dto.AuthenticationDto
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthenticationController::class)
internal class AuthenticationControllerTest : ControllerTest() {

    private val url = "/authenticate"

    @MockkBean
    private lateinit var userService: UserService

    @Test
    internal fun `should authenticate a user`() {
        fixtures()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthenticationDto("jon", "passwd"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data") { value("123") }
        }
    }

    @Test
    internal fun `should not authenticate a user - jwt singing error`() {
        fixtures()
        every { jwtService.sign(fixtureInquirer) } returns left(Declination.withReason("boom!"))

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthenticationDto("jon", "passwd"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    internal fun `should not authenticate a user - invalid password`() {
        fixtures()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthenticationDto("jon", "passwdX"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    internal fun `should not authenticate a user - invalid username`() {
        every { userService.loadUserByUsername(any()) } throws UsernameNotFoundException("nope")

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthenticationDto("jonX", "passwd"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    internal fun `should not authenticate a user - blank login and password`() {
        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthenticationDto("", ""))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    private fun fixtures() {
        every { userService.find("jon") } returns fixtureInquirer
        every { userService.loadUserByUsername(any()) } returns User(
            "jon",
            "\$2y\$12\$4C3av3VYh/8CW7ITlH8Yeeza12Q9QR5QdWV04S4HcS896w0l0yBq.",
            emptySet()
        )
        every { jwtService.sign(fixtureInquirer) } returns right("123")
    }
}