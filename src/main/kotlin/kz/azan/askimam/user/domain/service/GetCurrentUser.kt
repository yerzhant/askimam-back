package kz.azan.askimam.user.domain.service

import io.vavr.control.Option
import kz.azan.askimam.user.domain.model.User

fun interface GetCurrentUser {
    operator fun invoke(): Option<User>
}
