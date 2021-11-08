package com.example.artesano

import android.text.TextUtils
import android.util.Patterns

class SomeValidations {

    //validar email
    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}