package kz.azan.askimam.event.infra

import kz.azan.askimam.user.infra.FcmTokenDao
import kz.azan.askimam.user.infra.UserDao
import kz.azan.askimam.user.infra.askImamDbRole
import org.springframework.stereotype.Service

@Service
class GetImamsFcmTokensService(
    private val userDao: UserDao,
    private val fcmTokenDao: FcmTokenDao,
) {
    operator fun invoke() = userDao.findAllByRole(askImamDbRole)
        .flatMap { fcmTokenDao.findByUserId(it.id) }
        .map { it.value }
}
