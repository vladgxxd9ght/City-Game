package com.example.citygame

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val historyList = mutableListOf<String>()
    private val usedCities = mutableSetOf<String>()
    private lateinit var adapter: CityAdapter
    private lateinit var recyclerView: RecyclerView
    private var requiredLetter: Char? = null

    private val computerCities = listOf(
        "Астрахань", "Архангельск", "Барнаул", "Брянск", "Владивосток", "Воронеж",
        "Волгоград", "Грозный", "Екатеринбург", "Иркутск", "Ижевск", "Казань",
        "Калининград", "Краснодар", "Красноярск", "Курск", "Липецк", "Москва",
        "Мурманск", "Новгород", "Новосибирск", "Норильск", "Омск", "Орёл",
        "Пенза", "Пермь", "Псков", "Ростов", "Рязань", "Самара", "Саратов",
        "Смоленск", "Сочи", "Тамбов", "Тверь", "Томск", "Тула", "Тюмень",
        "Уфа", "Ульяновск", "Хабаровск", "Челябинск", "Чита", "Ярославль"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val editTextCity = findViewById<EditText>(R.id.editTextCity)
        val buttonSend = findViewById<Button>(R.id.buttonSend)
        recyclerView = findViewById(R.id.recyclerViewCities)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CityAdapter(historyList)
        recyclerView.adapter = adapter

        if (historyList.isEmpty()) {
            historyList.add("Игра началась! Назовите любой город.")
        }
        adapter.notifyDataSetChanged()

        buttonSend.setOnClickListener {
            val userCity = editTextCity.text.toString().trim()

            if (userCity.isEmpty()) {
                showAlert("Ошибка", "Введите название города!")
                return@setOnClickListener
            }

            val normalizedUserCity = userCity.lowercase()

            if (usedCities.contains(normalizedUserCity)) {
                showAlert("Повтор!", "Город «$userCity» уже был назван в этой игре.")
                return@setOnClickListener
            }

            val firstChar = normalizedUserCity.first()
            if (requiredLetter != null && firstChar != requiredLetter) {
                showAlert(
                    "Не та буква!",
                    "Вам нужно назвать город на букву «${requiredLetter!!.uppercaseChar()}»."
                )
                return@setOnClickListener
            }

            usedCities.add(normalizedUserCity)
            val lastUserLetter = getValidLastLetter(normalizedUserCity)
            historyList.add(userCity)

            val botCity = computerCities.firstOrNull { city ->
                val norm = city.lowercase()
                norm.first() == lastUserLetter && !usedCities.contains(norm)
            }

            if (botCity != null) {
                usedCities.add(botCity.lowercase())
                requiredLetter = getValidLastLetter(botCity.lowercase())
                historyList.add(botCity)
            } else {
                showAlert(
                    "Победа! 🎉",
                    "Компьютер не знает больше городов на букву «${lastUserLetter.uppercaseChar()}». Вы выиграли!"
                )
                requiredLetter = null
            }

            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(historyList.size - 1)
            editTextCity.text.clear()
        }
    }

    private fun getValidLastLetter(city: String): Char {
        val badLetters = setOf('ь', 'ъ', 'ы', 'й')
        for (i in city.length - 1 downTo 0) {
            if (city[i] !in badLetters) {
                return city[i]
            }
        }
        return city.last()
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_theme -> {
                val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

                if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                recreate()
            }
            R.id.menu_restart -> {
                historyList.clear()
                usedCities.clear()
                requiredLetter = null
                historyList.add("Игра началась заново! Назовите любой город.")
                adapter.notifyDataSetChanged()
            }
            R.id.menu_about -> {
                AlertDialog.Builder(this)
                    .setTitle("О программе")
                    .setMessage("Игра «Города» v1.0\n\nАвтор: ${getString(R.string.fio)}")
                    .setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }
                    .setIcon(R.mipmap.ic_launcher_round)
                    .create()
                    .show()
            }
            R.id.menu_exit -> {
                AlertDialog.Builder(this)
                    .setMessage("Вы действительно хотите выйти?")
                    .setCancelable(false)
                    .setPositiveButton("Да") { _, _ -> finish() }
                    .setNegativeButton("Нет") { dialog, _ -> dialog.cancel() }
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}