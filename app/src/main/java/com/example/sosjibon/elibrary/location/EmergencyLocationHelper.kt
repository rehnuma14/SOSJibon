package com.example.sosjibon.elibrary.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object EmergencyLocationHelper {

    private val emergencyNumbers = mapOf(
        "BD" to "999",  // Bangladesh
        "US" to "911",
        "CA" to "911",
        "GB" to "999",
        "IN" to "112",
        "AU" to "000",
        "DE" to "112",
        "FR" to "112",
        "JP" to "119",
        "CN" to "120",
        "PK" to "1122",
        "NP" to "102",
        "LK" to "119"
    )

    private const val DEFAULT_NUMBER = "112"

    fun getEmergencyNumber(countryCode: String?): String =
        emergencyNumbers[countryCode?.uppercase()] ?: DEFAULT_NUMBER

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCountryCode(context: Context): String? {
        if (!hasLocationPermission(context)) return null

        val location: Location? = suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { cont.resume(null) }
        }

        location ?: return null

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.firstOrNull()?.countryCode
            } catch (e: Exception) {
                null
            }
        }
    }

    fun launchDialer(context: Context, number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    }
}
