package com.example.tomtom.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionUtils {

    private const val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * Check if all location permissions are granted
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return locationPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Request location permissions
     */
    fun requestLocationPermissions(activity: FragmentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            locationPermissions,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Handle the result from the permission request
     */
    fun handlePermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
}
