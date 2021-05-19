package com.eddydunton.veronica

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.VibrationEffect

/**
 * Created to receive take shot requests from notifications
 */
class NotificationReceiver: BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        val rollindex = intent!!.getIntExtra("roll_index", -1)
        if (rollindex == -1) return // invalid index

        if (! MainActivity.hasLocationPermission()) return

        RollManager.rolls.get(rollindex).takeShot()
    }
}