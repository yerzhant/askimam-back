package kz.azan.askimam.security.web

import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import kz.azan.askimam.security.service.JwtService
import kz.azan.askimam.security.service.UserService
import kz.azan.askimam.security.web.dto.AuthenticationDto
import kz.azan.askimam.security.web.dto.AuthenticationResponseDto
import kz.azan.askimam.user.domain.model.User
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestApi("authenticate")
class AuthenticationController(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtService: JwtService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun auth(@Validated @RequestBody dto: AuthenticationDto) =
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(dto.login, dto.password)
            ).run {
                val user = userService.find(dto.login)
                val jwt = jwtService.sign(user)
                    .getOrElseThrow { declination ->
                        BadCredentialsException("Jwt signing error: ${declination.reason.value}")
                    }

                val userType = User.Type.valueOf(this.authorities.first().authority)

                ResponseDto.ok(AuthenticationResponseDto(jwt, user.id.value, userType))
            }
        } catch (e: BadCredentialsException) {
            logger.error("Authentication error: $e")
            throw e
        }
}