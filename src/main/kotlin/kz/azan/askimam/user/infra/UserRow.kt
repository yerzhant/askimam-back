package kz.azan.askimam.user.infra

import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table

private const val askImam = "ask-imam"

@Table("users")
data class UserRow(
    @Id
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val status: Int,
    val passwordHash: String,

    @MappedCollection(idColumn = "user_id")
    val roles: Set<AuthAssignmentRow>,
) {

    fun toDomain(tokens: Set<FcmTokenRow>) = User(
        id = User.Id(id),
        type = getType(),
        name = NonBlankString.of(name()),
        passwordHash = NonBlankString.of(passwordHash),
        fcmTokens = tokens.map { FcmToken.from(it.value) }.toMutableSet()
    )

    private fun name() = "$firstName $lastName"

    private fun getType() = if (roles.map { it.itemName }.any { it == askImam }) Imam else Inquirer
}
