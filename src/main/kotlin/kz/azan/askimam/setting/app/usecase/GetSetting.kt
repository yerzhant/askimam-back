package kz.azan.askimam.setting.app.usecase

import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.setting.domain.model.Setting.Key
import kz.azan.askimam.setting.domain.repo.SettingRepository

@UseCase
class GetSetting(private val repo: SettingRepository) {

    operator fun invoke(key: Key) = repo.findByKey(key)
}
