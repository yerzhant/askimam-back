package kz.azan.askimam.common.type

//TODO rename to NonBlank...
class NotBlankString private constructor(val value: String) {

    companion object {
        fun of(value: String): NotBlankString {
            require(value.isNotBlank())
            return NotBlankString(value.trim())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotBlankString

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "NotBlankString(value='$value')"
    }

}