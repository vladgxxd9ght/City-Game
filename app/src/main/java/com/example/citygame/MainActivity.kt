package com.example.citygame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val textViewAnswer = findViewById<TextView>(R.id.textViewAnswer)
        val editTextCity = findViewById<EditText>(R.id.editTextCity)
        val buttonSend = findViewById<Button>(R.id.buttonSend)


        buttonSend.setOnClickListener {
            val userCity = editTextCity.text.toString().trim()


            if (userCity.isEmpty()) {
                textViewAnswer.text = "Поле не должно быть пустым!"
            } else {

                textViewAnswer.text = "Ваш город: $userCity. Компьютер принял ход!"
                editTextCity.text.clear()
            }
        }
    }
}