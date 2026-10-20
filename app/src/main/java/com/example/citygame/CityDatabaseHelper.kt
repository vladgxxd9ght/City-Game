package com.example.citygame

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CityDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cities.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "cities"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DESC = "description"
        private const val COLUMN_IMAGE_RES = "image_res"
        private const val COLUMN_IMAGE_URI = "image_uri"
        private const val COLUMN_LAT = "latitude"
        private const val COLUMN_LON = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_DESC TEXT,
                $COLUMN_IMAGE_RES INTEGER,
                $COLUMN_IMAGE_URI TEXT,
                $COLUMN_LAT REAL,
                $COLUMN_LON REAL
            )
        """
        db?.execSQL(createTableQuery)

        // Добавим начальные города при первом запуске
        val defaultCities = listOf(
            City("Москва", "Столица России, мегаполис.", R.mipmap.ic_launcher, null, 55.751244, 37.618423),
            City("Санкт-Петербург", "Культурная столица на Неве.", R.mipmap.ic_launcher, null, 59.938630, 30.314130),
            City("Казань", "Город с богатой историей.", R.mipmap.ic_launcher, null, 55.796127, 49.106414)
        )

        for (city in defaultCities) {
            insertCityDirect(db, city)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun insertCityDirect(db: SQLiteDatabase?, city: City) {
        val values = ContentValues().apply {
            put(COLUMN_NAME, city.name)
            put(COLUMN_DESC, city.description)
            put(COLUMN_IMAGE_RES, city.imageResId)
            put(COLUMN_IMAGE_URI, city.imageUri)
            put(COLUMN_LAT, city.latitude)
            put(COLUMN_LON, city.longitude)
        }
        db?.insert(TABLE_NAME, null, values)
    }

    fun getAllCities(): MutableList<City> {
        val cityList = mutableListOf<City>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC))
                val imageRes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RES))
                val imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
                val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LAT))
                val lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LON))

                cityList.add(City(name, desc, imageRes, imageUri, lat, lon))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return cityList
    }

    fun addCity(city: City) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, city.name)
            put(COLUMN_DESC, city.description)
            put(COLUMN_IMAGE_RES, city.imageResId)
            put(COLUMN_IMAGE_URI, city.imageUri)
            put(COLUMN_LAT, city.latitude)
            put(COLUMN_LON, city.longitude)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun updateCityDescription(cityName: String, newDesc: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DESC, newDesc)
        }
        db.update(TABLE_NAME, values, "$COLUMN_NAME = ?", arrayOf(cityName))
        db.close()
    }

    fun updateCityPhoto(cityName: String, newUri: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IMAGE_URI, newUri)
        }
        db.update(TABLE_NAME, values, "$COLUMN_NAME = ?", arrayOf(cityName))
        db.close()
    }
}