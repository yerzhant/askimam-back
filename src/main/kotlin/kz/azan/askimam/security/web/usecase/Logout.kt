package kz.azan.askimam.security.web.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.security.web.dto.LogoutDto
import kz.azan.askimam.user.domain.repo.UserRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser

@UseCase
class Logout(
    private val currentUser: GetCurrentUser,
    private val userRepository: UserRepository,
) {

    operator fun invoke(dto: LogoutDto): Option<Declination> = currentUser().fold(
        { some(Declination.withReason("Who are you?")) },
        { userRepository.deleteToken(FcmToken.from(dto.fcmToken), it) }
    )
}
