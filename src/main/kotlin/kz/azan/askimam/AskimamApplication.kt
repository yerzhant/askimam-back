package kz.azan.askimam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AskimamApplication {

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}

fun main(args: Array<String>) {
    runApplication<AskimamApplication>(*args)
}

@ConstructorBinding
@ConfigurationProperties("app")
data class AppProperties(val jwt: Jwt) {
    data class Jwt(val key: String)
}