package kz.azan.askimam.setting.infra.repo

import io.vavr.control.Either
import io.vavr.kotlin.Try
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.setting.domain.model.Setting
import kz.azan.askimam.setting.domain.repo.SettingRepository
import kz.azan.askimam.setting.infra.dao.SettingDao
import org.springframework.stereotype.Component

@Component
class JdbcSettingRepository(private val dao: SettingDao) : SettingRepository {

    override fun findByKey(key: Setting.Key): Either<Declination, Setting> =
        Try { dao.findByKey(key) }
            .map {
                if (it.isEmpty) throw Exception("Key ${key.name} not found.")
                it.get()
            }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.toDomain() }
            )
}
