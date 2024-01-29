package kz.azan.askimam.security.web.usecase

import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.security.service.JwtService
import kz.azan.askimam.security.service.UserService
import kz.azan.askimam.security.web.dto.LoginDto
import kz.azan.askimam.security.web.dto.LoginResponseDto
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.repo.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

@UseCase
class Login(
    private val jwtService: JwtService,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    operator fun invoke(dto: LoginDto) = try {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(dto.login, dto.password)
        ).run {
            val user = userService.find(dto.login)
            val jwt = jwtService.sign(user)
                .getOrElseThrow { declination ->
                    BadCredentialsException("Jwt signing error: ${declination.reason.value}")
                }

            user.fcmTokens.add(FcmToken.from(dto.fcmToken))
            userRepository.saveTokens(user)

            val userType = User.Type.valueOf(authorities.first().authority)
            ResponseDto.ok(LoginResponseDto(jwt, user.id.value, userType, dto.fcmToken))
        }
    } catch (e: BadCredentialsException) {
        logger.error("Authentication error: $e, ${e.cause} for user: ${dto.login}")
        throw e
    }
}
