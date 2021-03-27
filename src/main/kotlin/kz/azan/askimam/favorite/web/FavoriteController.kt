package kz.azan.askimam.favorite.web

import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import kz.azan.askimam.favorite.app.usecase.GetMyFavorites
import kz.azan.askimam.favorite.web.dto.FavoriteDto
import org.springframework.web.bind.annotation.GetMapping

@RestApi("favorites")
class FavoriteController(
    private val getMyFavorites: GetMyFavorites,
) {
    @GetMapping
    fun getMy(): ResponseDto = getMyFavorites().fold(
        { ResponseDto.error(it.reason.value) },
        { list -> ResponseDto.ok(list.map { FavoriteDto.from(it) }.asJava()) }
    )
}