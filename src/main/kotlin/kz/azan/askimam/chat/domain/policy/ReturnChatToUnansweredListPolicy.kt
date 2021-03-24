package kz.azan.askimam.chat.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam

fun interface ReturnChatToUnansweredListPolicy {
    fun isAllowed(user: User): Option<Declination>

    companion object {
        val forAll = ReturnChatToUnansweredListPolicy { user ->
            if (user.type == Imam)
                none()
            else
                some(Declination.withReason("The operation is not permitted"))
        }
    }
}