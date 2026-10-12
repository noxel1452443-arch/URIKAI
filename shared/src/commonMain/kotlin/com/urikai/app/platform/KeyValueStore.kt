package com.urikai.app.platform

/** SharedPreferences 相当の永続 KV。Android は SharedPreferences、Web は localStorage。 */
interface KeyValueStore {
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long
    fun getString(key: String, default: String?): String?
    fun getStringSet(key: String, default: Set<String>?): Set<String>?
    fun contains(key: String): Boolean
    fun edit(): KeyValueEditor
}

interface KeyValueEditor {
    fun putBoolean(key: String, value: Boolean): KeyValueEditor
    fun putInt(key: String, value: Int): KeyValueEditor
    fun putLong(key: String, value: Long): KeyValueEditor
    fun putString(key: String, value: String?): KeyValueEditor
    fun putStringSet(key: String, value: Set<String>?): KeyValueEditor
    fun remove(key: String): KeyValueEditor
    fun apply()
}

/** メモリ上の KV（保存不可環境のフォールバック）。 */
class InMemoryKeyValueStore : KeyValueStore {
    private val map = mutableMapOf<String, Any>()

    override fun getBoolean(key: String, default: Boolean) = map[key] as? Boolean ?: default
    override fun getInt(key: String, default: Int) = map[key] as? Int ?: default
    override fun getLong(key: String, default: Long) = map[key] as? Long ?: default
    override fun getString(key: String, default: String?) = map[key] as? String ?: default

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, default: Set<String>?) = map[key] as? Set<String> ?: default
    override fun contains(key: String) = key in map

    override fun edit(): KeyValueEditor = object : KeyValueEditor {
        private val ops = mutableListOf<(MutableMap<String, Any>) -> Unit>()
        private fun op(block: (MutableMap<String, Any>) -> Unit): KeyValueEditor = apply { ops += block }
        override fun putBoolean(key: String, value: Boolean) = op { it[key] = value }
        override fun putInt(key: String, value: Int) = op { it[key] = value }
        override fun putLong(key: String, value: Long) = op { it[key] = value }
        override fun putString(key: String, value: String?) =
            op { if (value == null) it.remove(key) else it[key] = value }
        override fun putStringSet(key: String, value: Set<String>?) =
            op { if (value == null) it.remove(key) else it[key] = value.toSet() }
        override fun remove(key: String) = op { it.remove(key) }
        override fun apply() = ops.forEach { it(map) }
    }
}
