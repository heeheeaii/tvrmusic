interface ObjectStorage {
    fun <T> store(obj: T, path: String)
    fun <T> load(path: String, clazz: Class<T>): T?
}
