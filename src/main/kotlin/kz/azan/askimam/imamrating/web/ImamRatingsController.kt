package kz.azan.askimam.imamrating.web

import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.dto.ResponseDto.Companion.error
import kz.azan.askimam.common.web.dto.ResponseDto.Companion.ok
import kz.azan.askimam.common.web.meta.RestApi
import kz.azan.askimam.imamrating.app.usecase.GetImamRatings
import kz.azan.askimam.imamrating.web.dto.ImamRatingDto
import kz.azan.askimam.imamrating.web.dto.ImamRatingsWithDescriptionDto
import kz.azan.askimam.setting.app.usecase.GetSetting
import kz.azan.askimam.setting.domain.model.Setting.Key.AskImamImamRatingsDescription
import org.springframework.web.bind.annotation.GetMapping

@RestApi("imam-ratings")
class ImamRatingsController(
    private val getImamRatings: GetImamRatings,
    private val getSetting: GetSetting,
) {

    @GetMapping
    fun getIt(): ResponseDto = getImamRatings().fold(
        { error(it) },
        { list -> ok(list.map { ImamRatingDto.from(it) }.asJava()) }
    )

    @GetMapping("with-desc")
    fun getItWithDesc(): ResponseDto = getImamRatings().fold(
        { error(it) },
        { ratings ->
            getSetting(AskImamImamRatingsDescription).fold(
                { error(it) },
                { setting ->
                    ok(
                        ImamRatingsWithDescriptionDto(
                            setting.value,
                            ratings.map { ImamRatingDto.from(it) }.asJava(),
                        )
                    )
                }
            )
        }
    )
}
