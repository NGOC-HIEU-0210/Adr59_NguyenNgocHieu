class Bai4 {
    companion object{
        fun buildString(builderAction: StringBuilder.() -> Unit): String {
            val sb = StringBuilder()
            sb.builderAction()
            return sb.toString()
        }
        fun bai4(){
            val result = buildString({ append("Hello"); append(" World") })
            println(result)
        }
    }
}