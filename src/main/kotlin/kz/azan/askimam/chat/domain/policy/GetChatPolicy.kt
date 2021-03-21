package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer

fun interface GetChatPolicy {
    fun isAllowed(chat: Chat, user: User): Either<Declination, Chat>

    companion object {
        fun getFor(user: User) =
            when (user.type) {
                Imam -> forImam
                Inquirer -> forInquirer
            }

        val forImam = GetChatPolicy { chat, user ->
            if (user.type == Imam)
                right(chat)
            else
                left(Declination.withReason("The operation is not permitted"))
        }

        val forInquirer = GetChatPolicy { chat, user ->
            if (chat.isVisibleToPublic() || chat.askedBy == user.id)
                right(chat)
            else
                left(Declination.withReason("The operation is not permitted"))
        }
    }
}