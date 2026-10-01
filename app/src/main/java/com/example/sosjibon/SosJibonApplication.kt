/*
 * SosJibonApplication.kt
 * What this file does: App entry point initializing Firebase App Check with a fixed Version 4 UUID debug token.
 *
 * Pseudo-code:
 * 1. Store FIXED_DEBUG_SECRET ("f5568e00-1111-4222-8333-444455556666") in SharedPreferences.
 * 2. Initialize FirebaseApp and install DebugAppCheckProviderFactory.
 */

package com.example.sosjibon

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class SosJibonApplication : Application() {

    companion object {
        const val FIXED_DEBUG_SECRET = "f5568e00-1111-4222-8333-444455556666"
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val appCheckPrefs = getSharedPreferences("com.google.firebase.appcheck.debug.store", MODE_PRIVATE)
            appCheckPrefs.edit().putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", FIXED_DEBUG_SECRET).apply()

            FirebaseApp.initializeApp(this)
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

            Log.d("SOSJIBON_APP", "Firebase App Check initialized with Fixed Debug Secret Token: $FIXED_DEBUG_SECRET")
        } catch (e: Exception) {
            Log.e("SOSJIBON_APP", "Firebase App Check init failed: ${e.message}")
        }
    }
}
