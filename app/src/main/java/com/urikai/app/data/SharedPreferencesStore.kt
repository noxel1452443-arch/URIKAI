package com.urikai.app.data

import android.content.Context
import android.content.SharedPreferences
import com.urikai.app.platform.KeyValueEditor
import com.urikai.app.platform.KeyValueStore

class SharedPreferencesStore(context: Context, name: String) : KeyValueStore {

    private val prefs: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    override fun getInt(key: String, default: Int) = prefs.getInt(key, default)
    override fun getLong(key: String, default: Long) = prefs.getLong(key, default)
    override fun getString(key: String, default: String?): String? = prefs.getString(key, default)
    override fun getStringSet(key: String, default: Set<String>?): Set<String>? = prefs.getStringSet(key, default)
    override fun contains(key: String) = prefs.contains(key)
    override fun edit(): KeyValueEditor = Editor(prefs.edit())

    private class Editor(private val e: SharedPreferences.Editor) : KeyValueEditor {
        override fun putBoolean(key: String, value: Boolean) = apply { e.putBoolean(key, value) }
        override fun putInt(key: String, value: Int) = apply { e.putInt(key, value) }
        override fun putLong(key: String, value: Long) = apply { e.putLong(key, value) }
        override fun putString(key: String, value: String?) = apply { e.putString(key, value) }
        override fun putStringSet(key: String, value: Set<String>?) = apply { e.putStringSet(key, value) }
        override fun remove(key: String) = apply { e.remove(key) }
        override fun apply() = e.apply()
    }
}
