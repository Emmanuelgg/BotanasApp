package com.example.botanas.services

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class Gps(context: Context) {

    private val MINIMUM_DISTANCE_CHANGE_FOR_UPDATES: Float = 1F // in Meters
    private val MINIMUM_TIME_BETWEEN_UPDATES: Long = 1000 // in Milliseconds
    private var activity: Activity = context as Activity

    companion object {
        lateinit var context: Context
    }

    private lateinit var locationManager: LocationManager

    init {
        Gps.context = context

        locationManager =  context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocatePermission()
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            MINIMUM_TIME_BETWEEN_UPDATES,
            MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
            MyLocationListener()
        )

    }

    private fun checkLocatePermission() {
        if ( ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION) ,
                11
            )
        }
    }

    private fun showCurrentLocation() {
        checkLocatePermission()
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null) {
            val message = String.format(
                "Current Location \n Longitude: %1\$s \n Latitude: %2\$s",
                location.longitude, location.latitude
            )
            Toast.makeText(
                context, message,
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private class MyLocationListener: LocationListener {
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderEnabled(provider: String?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderDisabled(provider: String?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onLocationChanged(location: Location?) {
            val message = String.format(
                "New Location \n Longitude: %1\$s \n Latitude: %2\$s",
                location?.longitude, location?.latitude
            )
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }




}