package kz.azan.askimam.security.web.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.vavr.control.Either
import io.vavr.kotlin.left
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.web.dto.ResponseDto.Status.Ok
import kz.azan.askimam.security.service.JwtService
import kz.azan.askimam.security.service.UserService
import kz.azan.askimam.security.web.dto.LoginDto
import kz.azan.askimam.security.web.dto.LoginResponseDto
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

internal class LoginTest : ChatFixtures() {

    private val jwtService = mockk<JwtService>()

    private val userService = mockk<UserService>()

    private val authenticationManager = mockk<AuthenticationManager>()

    private val underTest = Login(jwtService, userService, userRepository, authenticationManager)

    @Test
    internal fun `should authenticate a user`() {
        fixtures()

        val result = underTest(
            LoginDto(
                fixtureInquirer.name.value,
                "password",
                fixtureInquirerFcmToken.value.value,
            )
        )

        assertThat(result.status).isEqualTo(Ok)
        assertThat((result.data as LoginResponseDto).jwt).isEqualTo("123")
        assertThat((result.data as LoginResponseDto).userId).isEqualTo(fixtureInquirerId.value)
        assertThat((result.data as LoginResponseDto).userType).isEqualTo(Inquirer)
        assertThat((result.data as LoginResponseDto).fcmToken).isEqualTo(fixtureInquirerFcmToken.value.value)

        verify {
            userRepository.saveTokens(
                User(
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
        every { authenticationManager.authenticate(any()) } returns
                UsernamePasswordAuthenticationToken(1, null, setOf(SimpleGrantedAuthority(Imam.name)))

        val result = underTest(
            LoginDto(
                fixtureImam.name.value,
                "password",
                fixtureImamFcmToken.value.value,
            )
        )

        assertThat(result.status).isEqualTo(Ok)
        assertThat((result.data as LoginResponseDto).jwt).isEqualTo("123")
        assertThat((result.data as LoginResponseDto).userId).isEqualTo(fixtureImamId.value)
        assertThat((result.data as LoginResponseDto).userType).isEqualTo(Imam)
        assertThat((result.data as LoginResponseDto).fcmToken).isEqualTo(fixtureImamFcmToken.value.value)

        verify {
            userRepository.saveTokens(
                User(
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

        assertThrows<BadCredentialsException> {
            underTest(
                LoginDto(
                    fixtureInquirer.name.value,
                    "password",
                    fixtureInquirerFcmToken.value.value,
                )
            )
        }
    }


    @Test
    internal fun `should not authenticate a user - tokens are not saved`() {
        fixtures()
        every { userRepository.saveTokens(any()) } throws Exception("nope")

        assertThrows<Exception> {
            underTest(
                LoginDto(
                    fixtureInquirer.name.value,
                    "password",
                    fixtureInquirerFcmToken.value.value,
                )
            )
        }
    }

    private fun fixtures() {
        val inquirersName = fixtureInquirer.name.value
        val imamsName = fixtureImam.name.value

        every { authenticationManager.authenticate(any()) } returns
                UsernamePasswordAuthenticationToken(1, null, setOf(SimpleGrantedAuthority(Inquirer.name)))

        every { userService.find(inquirersName) } returns fixtureInquirer
        every { userService.find(imamsName) } returns fixtureImam

        every { jwtService.sign(fixtureInquirer) } returns Either.right("123")
        every { jwtService.sign(fixtureImam) } returns Either.right("123")

        every { userRepository.saveTokens(any()) } returns Unit
    }
}
