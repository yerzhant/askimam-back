package kz.azan.askimam.user.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.repo.UserRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser

@UseCase
class UpdateFcmToken(
    private val getCurrentUser: GetCurrentUser,
    private val userRepository: UserRepository,
) {
    fun process(request: UpdateFcmTokenRequest): Option<Declination> = getCurrentUser().fold(
        { some(Declination.withReason("Who are you?")) },
        { user ->
            user.fcmTokens.remove(request.oldToken)
            user.fcmTokens.add(request.newToken)
            userRepository.saveTokens(user)
            none()
        }
    )
}