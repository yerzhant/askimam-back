package kz.azan.askimam.user.infra.model

import org.springframework.data.relational.core.mapping.Table

@Table("auth_assignment")
data class AuthAssignmentRow(
    val itemName: String,
    val userId: String,
)