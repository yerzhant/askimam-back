package kz.azan.askimam.common.web.meta

import org.springframework.core.annotation.AliasFor
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Target(AnnotationTarget.CLASS)
@RestController
@RequestMapping
annotation class RestApi(
    @get:AliasFor(annotation = RequestMapping::class)
    val value: String
)