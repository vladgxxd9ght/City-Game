package com.example.citygame

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbarSettings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val editTextName = findViewById<EditText>(R.id.editTextPlayerName)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)

        // Загружаем сохраненное имя
        val prefs = getSharedPreferences("GAME_SETTINGS", Context.MODE_PRIVATE)
        val savedName = prefs.getString("PLAYER_NAME", "")
        editTextName.setText(savedName)

        // Сохраняем по кнопке
        btnSave.setOnClickListener {
            val newName = editTextName.text.toString().trim()
            prefs.edit().putString("PLAYER_NAME", newName).apply()
            Toast.makeText(this, "Настройки сохранены!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}