package com.eddydunton.veronica

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.*
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    companion object {
        val NOTIFICATION_CHANNEL_ID = "veronica"
        lateinit var appContext: Context
        lateinit var locationManager: LocationManager
        lateinit var clipboardManager: ClipboardManager
        lateinit var notificationManager: NotificationManager
        lateinit var vibrator: Vibrator

        lateinit var notificationChannel: NotificationChannel

        private var notificationId = -1

        //Returns a unique notification id
        fun getNotificationId(): Int {
            notificationId ++
            return notificationId
        }

        //Checks if location permissions have been granted
        fun hasLocationPermission() =
            (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        //Creates notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Rolls",
                    IMPORTANCE_HIGH).apply {description =
                        "Allows user to keep track of rolls and record photos from a notification"}
            (this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(notificationChannel)
        }

        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
    }
}