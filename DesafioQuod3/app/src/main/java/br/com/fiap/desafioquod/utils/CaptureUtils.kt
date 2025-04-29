package br.com.fiap.desafioquod.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import android.os.Looper
import com.google.android.gms.location.*


// Função para obter a localização (agora com lambda como parâmetro)
fun getLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        onLocationReceived(null)
        return
    }

    // Configurar solicitação de localização
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 1000 // Intervalo de 1 segundo
        numUpdates = 1 // Solicitar apenas uma atualização
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            onLocationReceived(location)
            fusedLocationClient.removeLocationUpdates(this) // Parar após receber
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            if (!locationAvailability.isLocationAvailable) {
                onLocationReceived(null)
            }
        }
    }

    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            onLocationReceived(null)
        }
    } catch (e: Exception) {
        onLocationReceived(null)
    }

    // Fallback para lastLocation se a atualização demorar muito
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(location)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
// Função para obter metadados do dispositivo
fun getCaptureMetadata(context: Context): Map<String, String> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val captureDate = dateFormat.format(Date())

    return mapOf(
        "manufacturer" to Build.MANUFACTURER,
        "model" to Build.MODEL,
        "captureDate" to captureDate
    )
}