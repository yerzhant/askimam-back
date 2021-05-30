package kz.azan.askimam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableAsync
import java.time.Clock
import javax.sql.DataSource

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AskimamApplication {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties() = DataSourceProperties()

//    @Bean
//    @ConfigurationProperties("spring.search-datasource")
//    fun searchDataSource(): DataSource = DataSourceBuilder.create().build()
//
//    @Bean
//    fun searchJdbcTemplate() = JdbcTemplate(searchDataSource())

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