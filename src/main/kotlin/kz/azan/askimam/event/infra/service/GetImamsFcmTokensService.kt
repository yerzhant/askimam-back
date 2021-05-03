package kz.azan.askimam.event.infra.service

import kz.azan.askimam.user.infra.dao.FcmTokenDao
import kz.azan.askimam.user.infra.dao.UserDao
import kz.azan.askimam.user.infra.model.askImamDbRole
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