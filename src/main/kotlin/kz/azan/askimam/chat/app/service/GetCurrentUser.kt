package kz.azan.askimam.chat.app.service

import kz.azan.askimam.user.domain.model.User

fun interface GetCurrentUser {
    operator fun invoke(): User
}
