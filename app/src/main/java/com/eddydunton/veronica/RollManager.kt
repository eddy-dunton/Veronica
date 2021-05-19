package com.eddydunton.veronica

import android.location.Geocoder
import android.location.Location
import android.widget.Toast.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

object RollManager {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val geocoder = Geocoder(MainActivity.appContext)

    val rolls: LinkedList<Roll> = LinkedList<Roll>()
    init {
        this.loadRolls()
    }

    /**
     * Returns the names of all the rolls as a LinkedList
     */
    fun getNames() = this.rolls.map{value -> value.toString()}

    /**
     * Converts a name to a filename
     * Converts to lowercase and removes all nonalphanumeric characters#
     * Does NOT add .json to end
     */
    private fun convertToFileName(name: String) = name.toLowerCase(Locale.getDefault()).replace("\\W*", "")

    /**
     * Checks that the directory for film files exists, and if not creates one
     *
     * Then returns the file path for that film directory
     */
    private fun checkFilmDirectory(): File {
        //if (MainActivity.appContext == null) return File("FATAL ERROR") //Should be impossible

        val dir = File(MainActivity.appContext.filesDir, "film")
        if (! dir.isDirectory) dir.mkdir()
        return dir
    }

    private fun loadRolls() {
        val dir = this.checkFilmDirectory()
        val files = dir.listFiles()

        for (file in files) { //Iterate through all files in directory
            try {
                //Check that it is a JSON file
                if (!file.path.endsWith(".json", true)) continue

                val reader = BufferedReader(FileReader(file))
                val stringbuilder = StringBuilder()

                reader.forEachLine { line -> stringbuilder.append(line) }
                //Read every line of file into stringbuilder

                val json = JSONObject(stringbuilder.toString()) //Convert string builder into JSON object

                val name = json.getString("name")

                val shotsjson = json.getJSONArray("shots")
                val shots = LinkedList<Shot>()

                for (i in 0 until shotsjson.length()) {
                    shots.add(Shot.fromJSON(shotsjson.getJSONObject(i)))
                }

                this.rolls.add(Roll(name, file, shots))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //Adds a roll with the given name
    fun add(name: String) {
        var filename = convertToFileName(name)

        val json = JSONObject()
        json.put("name", name)
        json.put("shots", JSONArray())

        var file = File(checkFilmDirectory(), "$filename.json") //Create file
        while (! file.createNewFile()) { //Try to create file
            filename += "-"
            file = File(checkFilmDirectory(), "$filename.json") //Create file
        }

        //Write initial JSON to file
        val writer = BufferedWriter(FileWriter(file))
        writer.write(json.toString())
        writer.close()

        this.rolls.add(Roll(name, file, LinkedList<Shot>()))
    }

    //Deletes a roll, including its file and any open notifications
    fun delete(index: Int) {
        val roll = this.rolls.get(index)
        roll.file.delete()
        MainActivity.notificationManager.cancel(roll.notificationId)
        this.rolls.removeAt(index)
    }
}