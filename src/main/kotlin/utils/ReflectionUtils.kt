package utils

object ReflectionUtils {
    fun getInstance(clazz: Class<*>): Any? {
        return try {
            val field = clazz.getDeclaredField("INSTANCE")
            field.isAccessible = true
            field.get(null)
        }
        catch (_: NoSuchFieldException) {
            clazz.getDeclaredConstructor().newInstance()
        }
    }
}