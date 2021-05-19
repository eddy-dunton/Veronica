package com.eddydunton.veronica

import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.stream.Collectors


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class RollView : Fragment() {
    private lateinit var roll: Roll
    private lateinit var viewListShots: ListView
    private lateinit var viewTextShotCount: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.setHasOptionsMenu(true)

        val rollIndex = this.requireArguments().getInt("roll_index")
        this.roll = RollManager.rolls[rollIndex] //Gets roll
        this.roll.view = this

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_roll_view, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.roll.view = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewListShots = this.requireView().findViewById(R.id.list_shots)!!
        this.viewTextShotCount = this.requireView().findViewById(R.id.text_shot_count)!!

        this.updateShots()

        this.viewListShots.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            if (this.activity == null) return@OnItemLongClickListener true //This should be impossible, but do it anyway

            val builder = AlertDialog.Builder(this.activity)
            builder.setTitle("Delete shot?")
            builder.setPositiveButton("Ok") {dialog, i ->
                this@RollView.roll.remove(position)
            }
            builder.setNegativeButton("Cancel") {dialog, i -> dialog.cancel()}
            builder.show()

            return@OnItemLongClickListener true
        }

        this.requireView().findViewById<TextView>(R.id.text_name).text = this.roll.toString()

        this.requireView().findViewById<Button>(R.id.button_take_shot).setOnClickListener {
            //Check for permission
            if (!MainActivity.hasLocationPermission()) {
                Toast.makeText(
                    MainActivity.appContext,
                    "Error: Location permissions not granted",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener //If permissions are not received, return
            }


            MainActivity.vibrator.vibrate(VibrationEffect.createOneShot(100,
                    VibrationEffect.DEFAULT_AMPLITUDE))

            this@RollView.roll.takeShot()
        }

        this.requireView().findViewById<Button>(R.id.button_notification).setOnClickListener {
            this.roll.createNotification()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.shot_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_copy -> {
                this.copy()
                true
            }
            R.id.action_add -> {
                val args = Bundle(1)
                args.putInt("roll_index", RollManager.rolls.indexOf(this.roll))
                findNavController().navigate(R.id.action_ViewRoll_to_AddShot, args)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Updates the shot text view with the current number of shots in this.roll
     *
     * And the list view
     */
    fun updateShots() {
        this.viewTextShotCount.text = this.roll.getShotCountString()
        this.viewListShots.adapter = SimpleAdapter(this.requireContext(),
            this.roll.shots.stream().map { s -> s.toHashMap() }.collect(Collectors.toList()),
            R.layout.shot_list_view,
            arrayOf("time", "addr"),
            intArrayOf(R.id.shot_list_time, R.id.shot_list_addr))
    }

    /**
     * Exports the current rolls .json file
     *
     * TODO Fix this lmao
     */
    fun export() {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        if (this.roll.file.exists()) {
            intentShareFile.type = "text/plain"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$this.roll.file"))
            intentShareFile.putExtra(
                Intent.EXTRA_SUBJECT,
                "Sharing File..."
            )
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
            startActivity(Intent.createChooser(intentShareFile, "Share File"))
        }
    }

    /**
     * Copies the current roll to clipboard
     */
    private fun copy() {
        val clip = ClipData.newPlainText("Film data", this.roll.toExportString())
        MainActivity.clipboardManager.setPrimaryClip(clip)
        Toast.makeText(this.context, "Copied to Clipboard!", Toast.LENGTH_SHORT).show()
    }
}