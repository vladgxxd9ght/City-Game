package com.example.citygame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.citуgame.CityAdapter
class MainActivity : AppCompatActivity() {

    // Список, где будут храниться названные города
    private val citiesList = mutableListOf<String>()
    private lateinit var adapter: CityAdapter // Объявим адаптер

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextCity = findViewById<EditText>(R.id.editTextCity)
        val buttonSend = findViewById<Button>(R.id.buttonSend)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCities)

        // Настраиваем RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CityAdapter(citiesList)
        recyclerView.adapter = adapter

        // Обработка хода игрока
        buttonSend.setOnClickListener {
            val city = editTextCity.text.toString().trim()

            if (city.isNotEmpty()) {
                // Добавляем город в общий список
                citiesList.add("Игрок: $city")

                // Имитируем ответ компьютера (позже сделаем умнее)
                citiesList.add("Компьютер: [Ответ на букву ${city.takeLast(1).uppercase()}]")

                // Уведомляем список, что данные изменились, чтобы он обновился на экране
                adapter.notifyDataSetChanged()

                // Прокручиваем список в самый низ к последнему сообщению
                recyclerView.scrollToPosition(citiesList.size - 1)

                editTextCity.text.clear()
            }
        }
    }
}