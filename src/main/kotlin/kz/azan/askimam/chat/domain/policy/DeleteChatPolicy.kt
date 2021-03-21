package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam

fun interface DeleteChatPolicy {
    fun isAllowed(chat: Chat, user: User): Option<Declination>

    companion object {
        val forImam = DeleteChatPolicy { _, user ->
            if (user.type == Imam)
                none()
            else
                some(Declination.withReason("Only imams are allowed to fulfil this operation"))
        }

        val forInquirer = DeleteChatPolicy { chat, user ->
            if (chat.askedBy == user.id)
                none()
            else
                some(Declination.withReason("Operation is not permitted"))
        }
    }
}