package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam

fun interface GetUnansweredChatsPolicy {
    fun isAllowed(user: User): Option<Declination>

    companion object {
        val forAll = GetUnansweredChatsPolicy { user ->
            when (user.type) {
                Imam -> none()
                else -> some(Declination.withReason("You're now allowed to view unanswered chats"))
            }
        }
    }
}