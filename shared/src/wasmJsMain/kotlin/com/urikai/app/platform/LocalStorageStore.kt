package com.urikai.app.platform

private fun lsGet(key: String): String? = js("{ try { return window.localStorage.getItem(key); } catch (e) { return null; } }")

private fun lsSet(key: String, value: String): Unit = js("{ try { window.localStorage.setItem(key, value); } catch (e) {} }")

private fun lsRemove(key: String): Unit = js("{ try { window.localStorage.removeItem(key); } catch (e) {} }")

/** localStorage 上の KV。キーは `urikai/<name>/<key>`、値はすべて文字列で保存する。 */
class LocalStorageStore(name: String) : KeyValueStore {

    private val prefix = "urikai/$name/"

    private fun raw(key: String): String? = lsGet(prefix + key)

    override fun getBoolean(key: String, default: Boolean) = raw(key)?.toBooleanStrictOrNull() ?: default
    override fun getInt(key: String, default: Int) = raw(key)?.toIntOrNull() ?: default
    override fun getLong(key: String, default: Long) = raw(key)?.toLongOrNull() ?: default
    override fun getString(key: String, default: String?) = raw(key) ?: default
    override fun getStringSet(key: String, default: Set<String>?): Set<String>? =
        raw(key)?.let(::decodeSet) ?: default
    override fun contains(key: String) = raw(key) != null

    override fun edit(): KeyValueEditor = object : KeyValueEditor {
        private val ops = mutableListOf<() -> Unit>()
        private fun put(key: String, value: String?): KeyValueEditor = apply {
            ops += { if (value == null) lsRemove(prefix + key) else lsSet(prefix + key, value) }
        }
        override fun putBoolean(key: String, value: Boolean) = put(key, value.toString())
        override fun putInt(key: String, value: Int) = put(key, value.toString())
        override fun putLong(key: String, value: Long) = put(key, value.toString())
        override fun putString(key: String, value: String?) = put(key, value)
        override fun putStringSet(key: String, value: Set<String>?) = put(key, value?.let(::encodeSet))
        override fun remove(key: String) = put(key, null)
        override fun apply() = ops.forEach { it() }
    }

    private companion object {
        const val SEP = '\u001F'
        const val EMPTY_SET = "\u001E"

        fun encodeSet(set: Set<String>): String =
            if (set.isEmpty()) EMPTY_SET else set.joinToString(SEP.toString())

        fun decodeSet(raw: String): Set<String> =
            if (raw == EMPTY_SET) emptySet() else raw.split(SEP).toSet()
    }
}
