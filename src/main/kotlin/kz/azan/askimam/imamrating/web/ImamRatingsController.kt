package kz.azan.askimam.imamrating.web

import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.dto.ResponseDto.Companion.error
import kz.azan.askimam.common.web.dto.ResponseDto.Companion.ok
import kz.azan.askimam.common.web.meta.RestApi
import kz.azan.askimam.imamrating.app.usecase.GetImamRatings
import kz.azan.askimam.imamrating.web.dto.ImamRatingDto
import org.springframework.web.bind.annotation.GetMapping

@RestApi("imam-ratings")
class ImamRatingsController(private val getImamRatings: GetImamRatings) {

    @GetMapping
    fun getIt(): ResponseDto = getImamRatings().fold(
        { error(it) },
        { list -> ok(list.map { ImamRatingDto.from(it) }.asJava()) }
    )
}
