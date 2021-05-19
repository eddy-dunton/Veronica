package com.eddydunton.veronica

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception
import java.lang.NumberFormatException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

class AddShot : Fragment() {
    private lateinit var roll: Roll

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_shot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rollIndex = this.requireArguments().getInt("roll_index")
        this.roll = RollManager.rolls[rollIndex] //Gets roll

        super.onViewCreated(view, savedInstanceState)

        //Add button is pressed
        this.requireView().findViewById<FloatingActionButton>(R.id.button_add).setOnClickListener {
            val parentView = this@AddShot.requireView()

            //Get and validate time
            val timeAsText = parentView.findViewById<EditText>(R.id.text_time).text
            val time: LocalTime
            try {time = LocalTime.parse(timeAsText)}
            catch (e: DateTimeParseException) {
                Toast.makeText(this@AddShot.context, "Time Invalid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Get and validate date
            val dateAsText = parentView.findViewById<EditText>(R.id.text_date).text
            val date: LocalDate
            try {date = LocalDate.parse(dateAsText)}
            catch (e: DateTimeParseException) {
                Toast.makeText(this@AddShot.context, "Date Invalid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Convert both to a datetime
            val datetime = LocalDateTime.of(date, time)

            //Convert to latitude to a double and validate
            val latAsText = parentView.findViewById<EditText>(R.id.text_lat).text
            val lat: Double
            try {
                lat = latAsText.toString().toDouble()
                if (lat < -180f || lat > 180f) {
                    throw NumberFormatException()
                }
            }
            catch (e: NumberFormatException) {
                Toast.makeText(this@AddShot.context, "Latitude Invalid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Convert longitude to a double and validate
            val longAsText = parentView.findViewById<EditText>(R.id.text_long).text
            val long: Double
            try {
                long = longAsText.toString().toDouble()
                if (long < -180f || long > 180f) {
                    throw NumberFormatException()
                }
            }
            catch (e: NumberFormatException) {
                Toast.makeText(this@AddShot.context, "Longitude Invalid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //If it reaches here then all the values are correct
            val shot = Shot(datetime, lat, long)

            var i = 0
            //Finds the correct position for the shot in the list
            //Continues the datetime is after i
            while (datetime.compareTo(this.roll.shots[i].time) < 0) {
                i ++
                //Check if the final value has been reached
                if (i == this.roll.shots.size) break
            }

            this.roll.shots.add(i, shot)
            this.roll.save()

            //this.findNavController().popBackStack()
            Toast.makeText(this@AddShot.context, "Shot Added!", Toast.LENGTH_SHORT).show()
        }
    }
}