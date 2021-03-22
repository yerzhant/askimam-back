package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer

fun interface UpdateChatPolicy {
    fun isAllowed(chat: Chat, user: User): Option<Declination>

    companion object {
        fun getFor(user: User) = when (user.type) {
            Imam -> forImam
            Inquirer -> forInquirer
        }

        val forImam = UpdateChatPolicy { _, user ->
            if (user.type == Imam)
                none()
            else
                some(Declination.withReason("Only imams are allowed to do this"))
        }
        val forInquirer = UpdateChatPolicy { chat, user ->
            if (chat.askedBy == user.id)
                none()
            else
                some(Declination.withReason("You're not allowed to edit this chat"))
        }
    }
}