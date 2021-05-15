package kz.azan.askimam.setting.app.usecase

import io.mockk.every
import io.mockk.mockk
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.setting.domain.model.Setting
import kz.azan.askimam.setting.domain.model.Setting.Key.AskImamImamRatingsDescription
import kz.azan.askimam.setting.domain.repo.SettingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetSettingTest {

    private val repo = mockk<SettingRepository>()

    private val underTest = GetSetting(repo)

    @Test
    internal fun `should get a setting`() {
        every { repo.findByKey(AskImamImamRatingsDescription) } returns right(
            Setting(AskImamImamRatingsDescription, "Description")
        )

        val result = underTest(AskImamImamRatingsDescription)

        assertThat(result.get().value).isEqualTo("Description")
    }

    @Test
    internal fun `should not get a setting`() {
        every { repo.findByKey(AskImamImamRatingsDescription) } returns left(Declination.withReason("x"))

        val result = underTest(AskImamImamRatingsDescription)

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
