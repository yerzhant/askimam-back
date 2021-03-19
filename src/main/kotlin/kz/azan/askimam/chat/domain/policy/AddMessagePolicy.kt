package kz.azan.askimam.chat.domain.policy

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam

fun interface AddMessagePolicy {
    fun isAllowedToAddMessage(user: User, chat: Chat): Boolean

    companion object {
        val imam = AddMessagePolicy { user, _ -> user.type == Imam }
        val inquirer = AddMessagePolicy { user, chat -> user.id == chat.askedBy }
    }
}