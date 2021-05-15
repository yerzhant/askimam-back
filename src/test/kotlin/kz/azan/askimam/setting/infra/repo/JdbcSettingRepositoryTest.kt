package kz.azan.askimam.setting.infra.repo

import io.mockk.every
import io.mockk.mockk
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.setting.domain.model.Setting.Key.AskImamImamRatingsDescription
import kz.azan.askimam.setting.infra.dao.SettingDao
import kz.azan.askimam.setting.infra.model.SettingRow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JdbcSettingRepositoryTest {

    private val dao = mockk<SettingDao>()

    private val underTest = JdbcSettingRepository(dao)

    @Test
    internal fun `should get a setting`() {
        every { dao.findByKey(AskImamImamRatingsDescription) } returns some(
            SettingRow(AskImamImamRatingsDescription, "Desc")
        )

        val result = underTest.findByKey(AskImamImamRatingsDescription)

        assertThat(result.get().value).isEqualTo("Desc")
    }

    @Test
    internal fun `should not find a setting`() {
        every { dao.findByKey(AskImamImamRatingsDescription) } returns none()

        val result = underTest.findByKey(AskImamImamRatingsDescription)

        assertThat(result.left.reason.value).isEqualTo("Key ${AskImamImamRatingsDescription.name} not found.")
    }

    @Test
    internal fun `should not get a setting`() {
        every { dao.findByKey(AskImamImamRatingsDescription) } throws Exception("x")

        val result = underTest.findByKey(AskImamImamRatingsDescription)

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
