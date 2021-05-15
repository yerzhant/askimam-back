package kz.azan.askimam.setting.app.usecase

import io.mockk.every
import io.mockk.mockk
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.setting.domain.model.Setting
import kz.azan.askimam.setting.domain.model.Setting.Key.AskImamImamRatingDescription
import kz.azan.askimam.setting.domain.repo.SettingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetSettingTest {

    private val repo = mockk<SettingRepository>()

    private val underTest = GetSetting(repo)

    @Test
    internal fun `should get a setting`() {
        every { repo.findByKey(AskImamImamRatingDescription) } returns right(
            Setting(AskImamImamRatingDescription, "Description")
        )

        val result = underTest(AskImamImamRatingDescription)

        assertThat(result.get().value).isEqualTo("Description")
    }

    @Test
    internal fun `should not get a setting`() {
        every { repo.findByKey(AskImamImamRatingDescription) } returns left(Declination.withReason("x"))

        val result = underTest(AskImamImamRatingDescription)

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
