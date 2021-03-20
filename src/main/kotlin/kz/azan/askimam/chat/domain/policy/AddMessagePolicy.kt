package kz.azan.askimam.chat.domain.policy

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam

fun interface AddMessagePolicy {
    fun isAllowed(chat: Chat, user: User): Boolean

    companion object {
        val forImam = AddMessagePolicy { _, user -> user.type == Imam }
        val forInquirer = AddMessagePolicy { chat, user -> user.id == chat.askedBy }
    }
}