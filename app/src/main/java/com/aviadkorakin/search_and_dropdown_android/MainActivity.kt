package com.aviadkorakin.search_and_dropdown_android

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aviadkorakin.search_and_dropdown_android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchDropdown.apply {
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
                // item is a Map<String, Any> – pull out whatever field you want:
                val picked = binding.searchDropdown.selectedItem
                Toast.makeText(
                    this@MainActivity,
                    "You picked: $picked",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}