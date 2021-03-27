package kz.azan.askimam.user.domain.service

import kz.azan.askimam.user.domain.model.User

fun interface GetCurrentUser {
    operator fun invoke(): User
}
