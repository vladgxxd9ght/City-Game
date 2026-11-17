package com.example.citygame

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

class CityWidgetConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.city_widget_configure)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // Загружаем города из БД
        val dbHelper = CityDatabaseHelper(this)
        val cityNames = dbHelper.getAllCities().map { it.name }

        val spinner = findViewById<Spinner>(R.id.spinnerCities)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        findViewById<Button>(R.id.btnAddWidget).setOnClickListener {
            val selectedCityName = spinner.selectedItem.toString()

            // Сохраняем выбор для ЭТОГО конкретного виджета
            val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("city_name_$appWidgetId", selectedCityName).apply()

            // Обновляем виджет
            val appWidgetManager = AppWidgetManager.getInstance(this)
            CityWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}