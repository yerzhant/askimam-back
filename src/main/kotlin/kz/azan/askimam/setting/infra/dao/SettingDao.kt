package kz.azan.askimam.setting.infra.dao

import io.vavr.control.Option
import kz.azan.askimam.setting.domain.model.Setting.Key
import kz.azan.askimam.setting.infra.model.SettingRow
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface SettingDao : CrudRepository<SettingRow, Key> {
    fun findByKey(key: Key): Option<SettingRow>
}
