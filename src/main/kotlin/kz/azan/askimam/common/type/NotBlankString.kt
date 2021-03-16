package kz.azan.askimam.common.type

class NotBlankString private constructor(val value: String) {
    companion object {
        fun of(value: String): NotBlankString {
            require(value.isNotBlank())
            return NotBlankString(value.trim())
        }
    }
}