package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User

fun interface UpdateMessagePolicy {
    fun isAllowed(authorId: User.Id, user: User, type: Message.Type): Option<Declination>

    companion object {
        val forAll = UpdateMessagePolicy { authorId, user, type ->
            when {
                type == Message.Type.Audio -> some(Declination.withReason("An audio message may not be edited"))
                authorId == user.id -> none()
                else -> some(Declination.withReason("You're not allowed to edit someone else's message"))
            }
        }
    }
}