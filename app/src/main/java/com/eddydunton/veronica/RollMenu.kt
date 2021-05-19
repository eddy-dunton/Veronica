package com.eddydunton.veronica

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RollMenu : Fragment() {
    companion object {
        internal var menu: RollMenu? = null
            get() = field
    }

    lateinit var rolls: ListView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_roll_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RollMenu.menu = this

        this.context.let {
            this.rolls = requireView().findViewById<ListView>(R.id.rolls_list) //this should always exist

            this.rolls.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                //Called when a roll is clicked on
                val args = Bundle(1)
                args.putInt("roll_index", position)
                view.findNavController().navigate(R.id.action_MainMenu_to_ViewRoll, args)
            }
            this.updateList()
        }

        this.rolls.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            if (this.activity == null) return@OnItemLongClickListener true //This should be impossible, but do it anyway

            val builder = AlertDialog.Builder(this.activity)
            builder.setTitle("Delete roll?")
            builder.setPositiveButton("Ok") {dialog, i ->
                RollManager.delete(position)
                this@RollMenu.updateList()
            }
            builder.setNegativeButton("Cancel") {dialog, i -> dialog.cancel()}
            builder.show()

            return@OnItemLongClickListener true
        }

        this.requireView().findViewById<FloatingActionButton>(R.id.add).setOnClickListener { view ->
            if (this.activity == null) return@setOnClickListener //This should be impossible, but do it anyway

            val builder = AlertDialog.Builder(this.activity)
            val input = EditText(this.activity)
            builder.setTitle("Enter Roll Name")
            builder.setView(input)
            builder.setPositiveButton("Ok") {dialog, i ->
                RollManager.add(input.text.toString())
                this@RollMenu.updateList()
            }
            builder.setNegativeButton("Cancel") {dialog, i -> dialog.cancel()}
            builder.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RollMenu.menu = null
    }

    fun updateList() {
        val data = LinkedList<HashMap<String, String>>()
        for (roll in RollManager.rolls) {
            data.add(roll.getHashMap())
        }
        this.rolls.adapter = SimpleAdapter(this.requireContext(), data, R.layout.roll_list_view,
                arrayOf("name", "shots", "last"),
                intArrayOf(R.id.roll_list_name, R.id.roll_list_shots, R.id.roll_list_last))
    }
}