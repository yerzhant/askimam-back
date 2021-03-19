package kz.azan.askimam.user.domain.model

class User(
    val id: Id,
    val type: Type,
) {
    data class Id(val value: Long)

    enum class Type { Imam, Inquirer }
}
