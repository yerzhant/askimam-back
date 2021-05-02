package kz.azan.askimam.user.domain.model

import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.type.NonBlankString

data class User(
    val id: Id,
    val type: Type,
    val name: NonBlankString,
    val passwordHash: NonBlankString,
    val fcmTokens: MutableSet<FcmToken> = mutableSetOf(),
) {
    data class Id(val value: Long)
    enum class Type { Imam, Inquirer }
}
