package com.example.citygame

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import java.io.File

class CityWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_city)

            try {
                val dbHelper = CityDatabaseHelper(context)
                val cities = dbHelper.getAllCities()

                val randomCity = if (cities.isNotEmpty()) {
                    cities.random()
                } else {
                    City("Москва", "Столица России", R.mipmap.ic_launcher, null, 55.75, 37.61)
                }

                views.setTextViewText(R.id.widgetCityName, randomCity.name)
                views.setTextViewText(R.id.widgetCityDesc, randomCity.description)

                // Загрузка картинки (из URI галереи или стандартного ресурса)
                if (!randomCity.imageUri.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(randomCity.imageUri)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.widgetCityImage, bitmap)
                        } else {
                            views.setImageViewResource(R.id.widgetCityImage, R.mipmap.ic_launcher)
                        }
                    } catch (e: Exception) {
                        views.setImageViewResource(R.id.widgetCityImage, R.mipmap.ic_launcher)
                    }
                } else {
                    val resId = if (randomCity.imageResId != 0) randomCity.imageResId else R.mipmap.ic_launcher
                    views.setImageViewResource(R.id.widgetCityImage, resId)
                }

            } catch (e: Exception) {
                views.setTextViewText(R.id.widgetCityName, "Игра в Города")
                views.setTextViewText(R.id.widgetCityDesc, "Нажмите для открытия")
                views.setImageViewResource(R.id.widgetCityImage, R.mipmap.ic_launcher)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}