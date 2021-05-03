package kz.azan.askimam.security.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import io.vavr.control.Either.right
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import kz.azan.askimam.security.service.JwtService
import kz.azan.askimam.security.service.UserService
import kz.azan.askimam.security.web.dto.LoginDto
import kz.azan.askimam.security.web.dto.LogoutDto
import kz.azan.askimam.security.web.usecase.Login
import kz.azan.askimam.security.web.usecase.Logout
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import kz.azan.askimam.user.domain.model.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthenticationController::class)

internal class AuthenticationControllerTest : ControllerTest() {

    private val url = "/auth"

    @MockkBean
    private lateinit var userRepositoryMockBean: UserRepository

    @MockkBean
    private lateinit var logout: Logout

    @Test
    internal fun `should authenticate a user`() {
        fixtures()

        mvc.post("$url/login") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginDto(
                    fixtureInquirer.name.value,
                    "password",
                    fixtureInquirerFcmToken.value.value,
                )
            )
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data.jwt") { value("123") }
            jsonPath("\$.data.userId") { value(fixtureInquirerId.value) }
            jsonPath("\$.data.userType") { value(Inquirer.name) }
        }
    }

    @Test
    internal fun `should authenticate an imam`() {
        fixtures()

        mvc.post("$url/login") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginDto(
                    fixtureImam.name.value,
                    "password",
                    fixtureImamFcmToken.value.value,
                )
            )
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data.jwt") { value("123") }
            jsonPath("\$.data.userId") { value(fixtureImamId.value) }
            jsonPath("\$.data.userType") { value(Imam.name) }
        }
    }

    @Test
    internal fun `should not authenticate a user - jwt singing error`() {
        fixtures()
        every { jwtService.sign(fixtureInquirer) } returns left(Declination.withReason("boom!"))

        mvc.post("$url/login") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginDto(
                    fixtureInquirer.name.value,
                    "password",
                    fixtureInquirerFcmToken.value.value,
                )
            )
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    internal fun `should not authenticate a user - invalid password`() {
        fixtures()

        mvc.post("$url/login") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginDto(
                    fixtureInquirer.name.value,
                    "passwordX",
                    fixtureInquirerFcmToken.value.value,
                )
            )
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    internal fun `should not authenticate a user - invalid username`() {
        every { userService.loadUserByUsername(any()) } throws UsernameNotFoundException("nope")

        mvc.post("$url/login") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginDto(
                    fixtureInquirer.name.value,
                    "password",
                    fixtureInquirerFcmToken.value.value,
                )
            )
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    internal fun `should not authenticate a user - blank login and password`() {
        mvc.post("$url/login") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginDto("", "", "123"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @WithPrincipal
    internal fun `should log out a user`() {
        val logoutDto = LogoutDto(fixtureInquirerFcmToken.value.value)
        fixtures()
        every { logout(logoutDto) } returns none()

        mvc.post("$url/logout") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(logoutDto)
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }

        verify { logout(logoutDto) }
    }

    @Test
    @WithPrincipal
    internal fun `should not log out a user`() {
        val logoutDto = LogoutDto(fixtureInquirerFcmToken.value.value)
        fixtures()
        every { logout(logoutDto) } returns some(Declination.withReason("x"))

        mvc.post("$url/logout") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(logoutDto)
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    @WithPrincipal
    internal fun `should not log out a user - invalid dto`() {
        mvc.post("$url/logout") {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(LogoutDto(""))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    private fun fixtures() {
        val inquirersName = fixtureInquirer.name.value
        val imamsName = fixtureImam.name.value

        every { userService.loadUserByUsername(inquirersName) } returns User(
            inquirersName,
            fixturePasswordHash.value,
            setOf(SimpleGrantedAuthority(Inquirer.name))
        )
        every { userService.loadUserByUsername(imamsName) } returns User(
            imamsName,
            fixturePasswordHash.value,
            setOf(SimpleGrantedAuthority(Imam.name))
        )

        every { userService.find(inquirersName) } returns fixtureInquirer
        every { userService.find(imamsName) } returns fixtureImam

        every { jwtService.sign(fixtureInquirer) } returns right("123")
        every { jwtService.sign(fixtureImam) } returns right("123")

        every { userRepositoryMockBean.saveTokens(any()) } returns Unit
    }

    @TestConfiguration
    class Config(
        private val jwtService: JwtService,
        private val userService: UserService,
        private val userRepository: UserRepository,
        private val authenticationManager: AuthenticationManager,
    ) {
        @Bean
        fun login() = Login(jwtService, userService, userRepository, authenticationManager)
    }
}
