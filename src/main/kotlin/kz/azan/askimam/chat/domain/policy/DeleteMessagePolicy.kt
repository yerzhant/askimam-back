package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User

fun interface DeleteMessagePolicy {
    fun isAllowed(authorId: User.Id, user: User): Option<Declination>

    companion object {
        val forImam = DeleteMessagePolicy { _, user ->
            if (user.type == User.Type.Imam) {
                none()
            } else {
                some(Declination("This operation is only allowed to imams"))
            }
        }

        val forInquirer = DeleteMessagePolicy { authorId, user ->
            if (user.id == authorId) {
                none()
            } else {
                some(Declination("You're not allowed to delete someone else's message"))
            }
        }
    }
}