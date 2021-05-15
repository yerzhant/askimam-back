package kz.azan.askimam.setting.domain.repo

import io.vavr.control.Either
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.setting.domain.model.Setting
import kz.azan.askimam.setting.domain.model.Setting.Key

interface SettingRepository {
    fun findByKey(key: Key): Either<Declination, Setting>
}
