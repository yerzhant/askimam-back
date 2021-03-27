package kz.azan.askimam.user.infra

import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserRow(
    @Id
    val id: Long,

    val firstName: String,
    val lastName: String,

    @MappedCollection(idColumn = "user_id")
    val roles: Set<AuthAssignmentRow>,
) {
    private val askImam = "askimam"

    fun toDomain() = User(
        User.Id(id),
        getType(),
        NonBlankString.of(name()),
    )

    private fun name() = "$firstName $lastName"

    private fun getType() = if (roles.map { it.itemName }.any { it == askImam }) Imam else Inquirer
}
