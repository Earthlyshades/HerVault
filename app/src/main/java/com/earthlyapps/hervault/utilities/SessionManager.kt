package com.earthlyapps.hervault.utilities

import android.content.Context
import com.earthlyapps.hervault.models.User

class SessionManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        const val KEY_UID = "uid"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_LINKED_PARTNER_ID = "linkedPartnerId"
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    fun saveUser(user: User) {
        editor.putString(KEY_UID, user.uid)
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_ROLE, user.role)
        user.linkedPartnerId.let {
            editor.putString(KEY_LINKED_PARTNER_ID, it)
        }
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUser(): User? {
        return if (isLoggedIn()) {
            User(
                uid = sharedPreferences.getString(KEY_UID, "")!!,
                name = sharedPreferences.getString(KEY_NAME, "")!!,
                email = sharedPreferences.getString(KEY_EMAIL, "")!!,
                role = sharedPreferences.getString(KEY_ROLE, "")!!,
                linkedPartnerId = sharedPreferences.getString(KEY_LINKED_PARTNER_ID, null).toString()
            )
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}