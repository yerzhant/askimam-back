package kz.azan.askimam.user.domain.model

import kz.azan.askimam.common.type.NonBlankString

data class User(
    val id: Id,
    val type: Type,
    val name: NonBlankString,
    val passwordHash: NonBlankString,
) {
    data class Id(val value: Long)
    enum class Type { Imam, Inquirer }
}
