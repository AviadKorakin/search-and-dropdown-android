package com.aviadkorakin.search_and_dropdown_android

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.aviadkorakin.search_and_dropdown.SearchDropdownView

class MainActivity : AppCompatActivity() {

    private lateinit var searchDropdown: SearchDropdownView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchDropdown = findViewById(R.id.searchDropdown)

        // ─── Hook up the callbacks ────────────────────────────────────────────
        searchDropdown.apply {
            setOnSuccessListener { results ->
                Toast.makeText(
                    this@MainActivity,
                    "Got ${results.size} locations",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setOnErrorListener { err ->
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${err.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
            setOnItemSelectedListener { item ->
                Toast.makeText(
                    this@MainActivity,
                    "You picked: $selectedItem",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}