package com.example.citygame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.ViewGroup

class CitiesActivity : AppCompatActivity() {

    private lateinit var dbHelper: CityDatabaseHelper
    private val citiesList = mutableListOf<City>()

    private lateinit var adapter: CityListAdapter
    private lateinit var recyclerView: RecyclerView

    private var targetCityForPhoto: City? = null
    private var newCityImageUri: String? = null
    private var tempThumbImageView: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri ->
            val uriString = imageUri.toString()
            if (targetCityForPhoto != null) {
                targetCityForPhoto?.imageUri = uriString
                targetCityForPhoto?.let { dbHelper.updateCityPhoto(it.name, uriString) }
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Фото обновлено в БД!", Toast.LENGTH_SHORT).show()
                targetCityForPhoto = null
            } else {
                newCityImageUri = uriString
                tempThumbImageView?.setImageURI(imageUri)
                Toast.makeText(this, "Фото выбрано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cities)

        dbHelper = CityDatabaseHelper(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCities)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewCitiesList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Загружаем данные из базы данных при старте
        loadCitiesFromDatabase()

        adapter = CityListAdapter(citiesList) { city ->
            showCityDetailsDialog(city)
        }
        recyclerView.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fabAddCity)
        fab.setOnClickListener {
            showAddCityDialog()
        }
    }

    private fun loadCitiesFromDatabase() {
        citiesList.clear()
        citiesList.addAll(dbHelper.getAllCities())
    }

    private fun showAddCityDialog() {
        val context = this
        newCityImageUri = null

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        tempThumbImageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.mipmap.ic_launcher)
            setPadding(0, 0, 0, 16)
        }

        val btnSelectPhoto = Button(context).apply {
            text = "Выбрать фото из галереи"
            setOnClickListener {
                targetCityForPhoto = null
                pickImageLauncher.launch("image/*")
            }
        }

        val inputName = EditText(context).apply { hint = "Название города" }
        val inputDesc = EditText(context).apply { hint = "Описание" }
        val inputLat = EditText(context).apply {
            hint = "Широта (например, 55.75)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        val inputLon = EditText(context).apply {
            hint = "Долгота (например, 37.61)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        layout.addView(tempThumbImageView)
        layout.addView(btnSelectPhoto)
        layout.addView(inputName)
        layout.addView(inputDesc)
        layout.addView(inputLat)
        layout.addView(inputLon)

        AlertDialog.Builder(context)
            .setTitle("Новый город")
            .setView(layout)
            .setPositiveButton("Добавить") { _, _ ->
                val name = inputName.text.toString().trim()
                val desc = inputDesc.text.toString().trim()
                val lat = inputLat.text.toString().toDoubleOrNull() ?: 0.0
                val lon = inputLon.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty()) {
                    val newCity = City(
                        name = name,
                        description = if (desc.isEmpty()) "Пользовательский город" else desc,
                        imageResId = R.mipmap.ic_launcher,
                        imageUri = newCityImageUri,
                        latitude = lat,
                        longitude = lon
                    )

                    // Сохраняем в БД
                    dbHelper.addCity(newCity)

                    // Перезагружаем список из БД
                    loadCitiesFromDatabase()
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(citiesList.size - 1)

                    Toast.makeText(context, "Город сохранен в базу данных!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Введите название города!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showCityDetailsDialog(city: City) {
        val context = this
        val dialogView = layoutInflater.inflate(R.layout.dialog_city_details, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.dialogImageCity)
        val descText = dialogView.findViewById<TextView>(R.id.dialogTextDescription)
        val coordsText = dialogView.findViewById<TextView>(R.id.dialogTextCoords)
        val linkMaps = dialogView.findViewById<TextView>(R.id.dialogLinkMaps)

        val btnEditDesc = dialogView.findViewById<Button>(R.id.dialogBtnEditDesc)
        val btnChangePhoto = dialogView.findViewById<Button>(R.id.dialogBtnChangePhoto)
        val btnClose = dialogView.findViewById<Button>(R.id.dialogBtnClose)

        // Устанавливаем данные
        if (city.imageUri != null) {
            imageView.setImageURI(Uri.parse(city.imageUri))
        } else {
            imageView.setImageResource(city.imageResId)
        }

        descText.text = city.description
        coordsText.text = "Широта: ${city.latitude}\nДолгота: ${city.longitude}"

        linkMaps.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:${city.latitude},${city.longitude}?q=${city.latitude},${city.longitude}(${Uri.encode(city.name)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            try {
                context.startActivity(mapIntent)
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${city.latitude},${city.longitude}"))
                context.startActivity(browserIntent)
            }
        }

        // Создаем диалог без системных кнопок
        val dialog = AlertDialog.Builder(context)
            .setTitle(city.name)
            .setView(dialogView)
            .create()

        // Обработчик кнопки "Изменить описание"
        btnEditDesc.setOnClickListener {
            showEditDescriptionDialog(city) { newDesc ->
                descText.text = newDesc // Мгновенно обновляем текст в открытом диалоге
            }
        }

        // Обработчик кнопки "Сменить фото"
        btnChangePhoto.setOnClickListener {
            targetCityForPhoto = city
            pickImageLauncher.launch("image/*")
            dialog.dismiss()
        }

        // Обработчик кнопки "Закрыть"
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Улучшенный метод редактирования с callback-ом для обновления UI
    private fun showEditDescriptionDialog(city: City, onUpdated: (String) -> Unit) {
        val context = this
        val input = EditText(context).apply {
            hint = "Введите новое описание"
            setText(city.description)
        }

        AlertDialog.Builder(context)
            .setTitle("Редактировать описание")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newDesc = input.text.toString().trim()
                if (newDesc.isNotEmpty()) {
                    dbHelper.updateCityDescription(city.name, newDesc)
                    city.description = newDesc
                    loadCitiesFromDatabase()
                    adapter.notifyDataSetChanged()

                    onUpdated(newDesc) // Передаем новое описание в открытый диалог деталей
                    Toast.makeText(context, "Описание обновлено в БД!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun showEditDescriptionDialog(city: City) {
        val context = this
        val input = EditText(context).apply {
            hint = "Введите новое описание"
            setText(city.description)
        }

        AlertDialog.Builder(context)
            .setTitle("Редактировать описание")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newDesc = input.text.toString().trim()
                if (newDesc.isNotEmpty()) {
                    // Обновляем в БД
                    dbHelper.updateCityDescription(city.name, newDesc)

                    // Перезагружаем список
                    loadCitiesFromDatabase()
                    adapter.notifyDataSetChanged()

                    Toast.makeText(context, "Описание обновлено в БД!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}