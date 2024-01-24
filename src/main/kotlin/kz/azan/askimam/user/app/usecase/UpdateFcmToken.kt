package kz.azan.askimam.user.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.repo.UserRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser

@UseCase
class UpdateFcmToken(
    private val getCurrentUser: GetCurrentUser,
    private val userRepository: UserRepository,
) {
    fun process(old: FcmToken, new: FcmToken): Option<Declination> = getCurrentUser().fold(
        { some(Declination.withReason("Who are you?")) },
        { user ->
            user.fcmTokens.remove(old)
            user.fcmTokens.add(new)
            userRepository.saveTokens(user)
            none()
        }
    )
}