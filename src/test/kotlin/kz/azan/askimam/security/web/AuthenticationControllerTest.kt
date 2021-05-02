package kz.azan.askimam.security.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import io.vavr.control.Either.right
import io.vavr.kotlin.left
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.security.web.dto.LoginDto
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import kz.azan.askimam.user.domain.model.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.web.servlet.post
import kz.azan.askimam.user.domain.model.User as AzanUser

@WebMvcTest(AuthenticationController::class)
internal class AuthenticationControllerTest : ControllerTest() {

    private val url = "/auth"

    @MockkBean
    private lateinit var userRepositoryMockBean: UserRepository

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

        verify {
            userRepositoryMockBean.saveTokens(
                AzanUser(
                    fixtureInquirerId,
                    fixtureInquirer.type,
                    fixtureInquirer.name,
                    fixturePasswordHash,
                    mutableSetOf(fixtureInquirerFcmToken),
                )
            )
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

        verify {
            userRepositoryMockBean.saveTokens(
                AzanUser(
                    fixtureImamId,
                    fixtureImam.type,
                    fixtureImam.name,
                    fixturePasswordHash,
                    mutableSetOf(fixtureImamFcmToken),
                )
            )
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
    internal fun `should not authenticate a user - tokens are not saved`() {
        fixtures()
        every { userRepositoryMockBean.saveTokens(any()) } throws Exception("nope")

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
}
