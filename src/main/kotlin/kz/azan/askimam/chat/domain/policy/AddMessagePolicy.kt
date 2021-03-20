package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam

fun interface AddMessagePolicy {
    fun isAllowed(chat: Chat, user: User): Option<Declination>

    companion object {
        val forImam = AddMessagePolicy { _, user ->
            if (user.type == Imam) {
                none()
            } else {
                some(Declination("This operation is only allowed to imams"))
            }
        }

        val forInquirer = AddMessagePolicy { chat, user ->
            if (user.id == chat.askedBy) {
                none()
            } else {
                some(Declination("You're not allowed to add a message to someone else's chat"))
            }
        }
    }
}