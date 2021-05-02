package kz.azan.askimam.user.infra

import kz.azan.askimam.chat.domain.model.FcmToken
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("fcm_tokens")
data class FcmTokenRow(
    @Id
    val value: String,
    val userId: Long,
) : Persistable<String> {

    companion object {
        fun from(token: FcmToken, userId: Long) = FcmTokenRow(
            token.value.value,
            userId,
        )
    }

    override fun getId() = value

    override fun isNew() = true
}
