package com.eddydunton.veronica

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.HashMap

data class Roll (val name: String, val file: File, val shots: LinkedList<Shot>): Comparable<Roll> {
    companion object {
        val criteria: Criteria = Criteria()

        init {
            this.criteria.accuracy = Criteria.ACCURACY_COARSE
            this.criteria.isAltitudeRequired = false
            this.criteria.isBearingRequired = false
            this.criteria.isCostAllowed = true
            this.criteria.horizontalAccuracy = Criteria.ACCURACY_LOW
            this.criteria.powerRequirement = Criteria.POWER_HIGH
            this.criteria.isSpeedRequired = false
        }
    }

    var view: RollView? = null //The current view this roll has
    val notificationId = MainActivity.getNotificationId()

    override fun toString() = this.name

    /**
     * Compares this roll to another
     *
     * Orders according to the date the last shot was taken
     *
     * The other roll is automatically returned if either is empty
     *
     * @return Positive int if this roll comes first, 0 if both are empty, negative int if other comes first
     */
    override fun compareTo(other: Roll): Int {
        if (this.shots.isEmpty()) {
            if (other.shots.isEmpty()) return 0
            else return 1
        } else {
            if (other.shots.isEmpty()) return -1
            return -this.shots.first.time.compareTo(other.shots.first.time)
        }
    }

    /**
     * Starts to request location in order for a shot to be taken
     * Does not however check for permissions
     */
    @SuppressLint("MissingPermission") //This is done
    fun takeShot() {
        MainActivity.locationManager.requestLocationUpdates(
                MainActivity.locationManager.getBestProvider(Roll.criteria, true)!!,
                -1, -1f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        @Suppress("SENSELESS_COMPARISON") //Not senseless thanks
                        if (location != null) {
                            this@Roll.recordShot(location)
                            MainActivity.locationManager.removeUpdates(this)
                        }
                    }
                })
    }

    fun recordShot(location: Location) {
        this.shots.addFirst(
                Shot(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(location.time),
                                ZoneId.systemDefault()
                        ),
                        location.latitude, location.longitude))

        this.update()
    }

    fun remove(index: Int) {
        shots.removeAt(index)
        this.update()
    }

    fun update() {
        this.save()
        this.view?.updateShots()
        RollMenu.menu?.updateList()

        //Update notification if it exists
        for (notification in MainActivity.notificationManager.activeNotifications) {
            if (notification.id == this.notificationId) {
                this.createNotification()
                break
            }
        }
    }

    fun save() {
        try {
            val writer = BufferedWriter(FileWriter(file))
            writer.write(this.toJSON().toString())
            writer.close()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(MainActivity.appContext, "Error saving roll", Toast.LENGTH_SHORT).show()
        }
    }

    fun createNotification() {
        with (NotificationManagerCompat.from(MainActivity.appContext)) {
            //Builds notification
            val takeShotIntent = Intent(MainActivity.appContext, NotificationReceiver::class.java).apply {
                action = Intent.ACTION_INSERT
                putExtra("roll_index", RollManager.rolls.indexOf(this@Roll))
            }

            val takeShotPendingIntent =
                PendingIntent.getBroadcast(MainActivity.appContext, MainActivity.getNotificationId(), takeShotIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val takeShotAction = Notification.Action(
                    R.drawable.notification_action_icon, "Take shot", takeShotPendingIntent)

            val openAppIntent = Intent(MainActivity.appContext, MainActivity::class.java)

            val openAppPendingIntent = PendingIntent.getActivity(
                    MainActivity.appContext, MainActivity.getNotificationId(), openAppIntent, 0)

            val notificationBuilder = Notification.Builder(MainActivity.appContext,
                MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(this@Roll.name)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(openAppPendingIntent)
                .addAction(takeShotAction)
                .setStyle(
                    Notification.MediaStyle()
                    .setShowActionsInCompactView(0))
                .setContentText(this@Roll.getShotCountString())

            this.notify(this@Roll.notificationId, notificationBuilder.build())
        }
    }

    private fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", this.name)
        val jsonShots = JSONArray()
        for (shot in this.shots) {
            jsonShots.put(shot.toJSON())
        }
        json.put("shots", jsonShots)
        return json
    }

    /**
     * Converts to a human readable string that can be exported
     */
    fun toExportString(): String {
        val out = StringBuilder()
        out.append("Film: ").append(this.name).append("\n\n")
        var count = 1
        for (shot in this.shots.asReversed()) {
            out.append(count).append("\t")
            out.append(RollManager.dateTimeFormatter.format(shot.time)).append(": ")
            out.append(shot.lat).append(", ")
            out.append(shot.lng).append("\n")

            count ++
        }

        return out.toString()
    }

    fun getHashMap(): HashMap<String, String> {
        val out = HashMap<String, String>()
        out.put("name", this.name)
        out.put("shots", this.getShotCountString())
        if (this.shots.isNotEmpty()) out.put("last", RollManager.dateTimeFormatter.format(this.shots.first.time))
        else out.put("last", "Unavailable")
        return out
    }

    fun getShotCountString() = if (this.shots.size == 1) "1 shot" else shots.size.toString() + " shots"

    fun getNotificationText() =
        if (this.shots.isNotEmpty())
                (this.getShotCountString() + "\t\t\n" + RollManager.dateTimeFormatter.format(this.shots.first.time))
        else this.getShotCountString()
}