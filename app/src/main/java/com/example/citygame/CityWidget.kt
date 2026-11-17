package com.example.citygame

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class CityWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_RANDOM_CITY = "com.example.citygame.ACTION_RANDOM_CITY"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

            // Берем сохраненный город или случайный, если ничего не сохранено
            val savedCityName = prefs.getString("city_name_$appWidgetId", null)
            val dbHelper = CityDatabaseHelper(context)
            val allCities = dbHelper.getAllCities()

            if (allCities.isEmpty()) return

            val city = if (savedCityName != null) {
                allCities.find { it.name == savedCityName } ?: allCities.random()
            } else {
                allCities.random()
            }

            val views = RemoteViews(context.packageName, R.layout.city_widget)
            views.setTextViewText(R.id.widgetCityName, city.name)
            views.setTextViewText(R.id.widgetCityDesc, city.description)
            views.setImageViewResource(R.id.widgetCityImage, city.imageResId)

            // 1. Клик по карточке -> Открыть MainActivity
            val openGameIntent = Intent(context, MainActivity::class.java)
            val openGamePendingIntent = PendingIntent.getActivity(
                context, 0, openGameIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widgetClickableArea, openGamePendingIntent)

            // 2. Клик по кнопке -> Сменить город (Broadcast)
            val randomIntent = Intent(context, CityWidget::class.java).apply {
                action = ACTION_RANDOM_CITY
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val randomPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, randomIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widgetBtnRandom, randomPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Обработка нажатия на кнопку "Случайный город"
        if (intent.action == ACTION_RANDOM_CITY) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

                val dbHelper = CityDatabaseHelper(context)
                val allCities = dbHelper.getAllCities()
                if (allCities.isNotEmpty()) {
                    val randomCity = allCities.random()

                    // Сохраняем новый город в SharedPreferences
                    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("city_name_$appWidgetId", randomCity.name).apply()

                    // Обновляем UI виджета
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}