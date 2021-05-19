package com.eddydunton.veronica

import org.json.JSONObject
import java.time.LocalDateTime

data class Shot (val time: LocalDateTime, val lat: Double, val lng: Double) {
    companion object {
        fun fromJSON(json: JSONObject) : Shot {
            val dateTime = LocalDateTime.parse(json.getString("time"), RollManager.dateTimeFormatter)
            val lat = json.getDouble("lat")
            val lng = json.getDouble("lng")
            return Shot(dateTime, lat, lng)
        }
    }

    val addr: String

    init {
        val addrBuilder = StringBuilder()

        val results = RollManager.geocoder.getFromLocation(lat, lng, 1)
        if (results.count() > 0) {
            val result = results.get(0)

            //Get first address line
            if (result.thoroughfare != null) addrBuilder.append(result.thoroughfare)

            //Get locality
            if (result.locality != null) {
                if (addrBuilder.isNotEmpty()) addrBuilder.append(", ")
                addrBuilder.append(result.locality)
            }

            //Get region
            if (result.subAdminArea != null) {
                if (addrBuilder.isNotEmpty()) addrBuilder.append(", ")
                addrBuilder.append(result.subAdminArea)
            }
            addr = addrBuilder.toString()
        } else {
            addr = "Unable to get address"
        }
    }

    override fun toString() =
            addr + RollManager.dateTimeFormatter.format(this.time) + ": %.2f, %.2f".format(lat, lng)

    /**
     * Converts this shot into a JSON object
     */
    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("time", RollManager.dateTimeFormatter.format(this.time))
        json.put("lat", this.lat)
        json.put("lng", this.lng)
        return json
    }

    /**
     * Converts to a hash map for the list adapater
     */
    fun toHashMap(): HashMap<String, String> {
        val out = HashMap<String, String>()
        out.put("time", RollManager.dateTimeFormatter.format(this.time))
        out.put("addr", this.addr + ": %.3f, %.3f".format(this.lat, this.lng))
        return out
    }
}