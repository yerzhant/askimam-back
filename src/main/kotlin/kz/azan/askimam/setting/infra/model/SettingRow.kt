package kz.azan.askimam.setting.infra.model

import kz.azan.askimam.setting.domain.model.Setting
import kz.azan.askimam.setting.domain.model.Setting.Key
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("settings")
data class SettingRow(
    @Id
    val key: Key,
    val value: String,
) {
    fun toDomain() = Setting(key, value)
}
