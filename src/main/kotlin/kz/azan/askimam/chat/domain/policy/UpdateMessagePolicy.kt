package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User

fun interface UpdateMessagePolicy {
    fun isAllowed(authorId: User.Id, user: User): Option<Declination>

    companion object {
        // TODO: additionally check that only text messages may be updated
        val forAll = UpdateMessagePolicy { authorId, user ->
            if (authorId == user.id)
                none()
            else
                some(Declination.withReason("You're not allowed to edit someone else's message"))
        }
    }
}