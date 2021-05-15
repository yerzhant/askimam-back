package kz.azan.askimam.setting.domain.model

data class Setting(
    val key: Key,
    val value: String,
) {
    enum class Key { AskImamImamRatingDescription }
}
