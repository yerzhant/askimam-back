package kz.azan.askimam.setting.infra.repo

import io.mockk.every
import io.mockk.mockk
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.setting.domain.model.Setting.Key.AskImamImamRatingDescription
import kz.azan.askimam.setting.infra.dao.SettingDao
import kz.azan.askimam.setting.infra.model.SettingRow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JdbcSettingRepositoryTest {

    private val dao = mockk<SettingDao>()

    private val underTest = JdbcSettingRepository(dao)

    @Test
    internal fun `should get a setting`() {
        every { dao.findByKey(AskImamImamRatingDescription) } returns some(
            SettingRow(AskImamImamRatingDescription, "Desc")
        )

        val result = underTest.findByKey(AskImamImamRatingDescription)

        assertThat(result.get().value).isEqualTo("Desc")
    }

    @Test
    internal fun `should not find a setting`() {
        every { dao.findByKey(AskImamImamRatingDescription) } returns none()

        val result = underTest.findByKey(AskImamImamRatingDescription)

        assertThat(result.left.reason.value).isEqualTo("Key ${AskImamImamRatingDescription.name} not found.")
    }

    @Test
    internal fun `should not get a setting`() {
        every { dao.findByKey(AskImamImamRatingDescription) } throws Exception("x")

        val result = underTest.findByKey(AskImamImamRatingDescription)

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
