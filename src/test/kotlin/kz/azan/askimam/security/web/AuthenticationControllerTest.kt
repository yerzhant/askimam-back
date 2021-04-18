package kz.azan.askimam.security.web

import io.mockk.every
import io.vavr.control.Either.right
import io.vavr.kotlin.left
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.security.web.dto.AuthenticationDto
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthenticationController::class)
internal class AuthenticationControllerTest : ControllerTest() {

    private val url = "/authenticate"

    @Test
    internal fun `should authenticate a user`() {
        fixtures()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthenticationDto("jon", "passwd"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data.jwt") { value("123") }
            jsonPath("\$.data.userType") { value(Inquirer.name) }
        }
    }

    @Test
    internal fun `should authenticate an imam`() {
        fixtures()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthenticationDto("imam", "passwd"))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data.jwt") { value("123") }
            jsonPath("\$.data.userType") { value(Imam.name) }
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
        every { userService.find("imam") } returns fixtureImam
        @Suppress("SpellCheckingInspection")
        every { userService.loadUserByUsername("jon") } returns User(
            "jon",
            "\$2y\$12\$4C3av3VYh/8CW7ITlH8Yeeza12Q9QR5QdWV04S4HcS896w0l0yBq.",
            setOf(SimpleGrantedAuthority(Inquirer.name))
        )
        @Suppress("SpellCheckingInspection")
        every { userService.loadUserByUsername("imam") } returns User(
            "imam",
            "\$2y\$12\$4C3av3VYh/8CW7ITlH8Yeeza12Q9QR5QdWV04S4HcS896w0l0yBq.",
            setOf(SimpleGrantedAuthority(Imam.name))
        )
        every { jwtService.sign(fixtureInquirer) } returns right("123")
        every { jwtService.sign(fixtureImam) } returns right("123")
    }
}