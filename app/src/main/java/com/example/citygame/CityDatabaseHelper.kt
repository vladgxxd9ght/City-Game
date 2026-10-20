package com.example.citygame

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CityDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cities.db"
        private const val DATABASE_VERSION = 6 // Подняли версию для обновления БД

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
        populateAllCities(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun populateAllCities(db: SQLiteDatabase?) {
        // Полный список городов с описаниями и координатами
        val allCities = listOf(
            arrayOf("Москва", "Столица России, мегаполис.", "55.751244", "37.618423"),
            arrayOf("Санкт-Петербург", "Культурная столица на Неве.", "59.938630", "30.314130"),
            arrayOf("Новосибирск", "Крупнейший город Сибири.", "55.008400", "82.935700"),
            arrayOf("Екатеринбург", "Столица Урала.", "56.838900", "60.605700"),
            arrayOf("Казань", "Город с богатой историей и культурой.", "55.796127", "49.106414"),
            arrayOf("Нижний Новгород", "Город на слиянии Оки и Волги.", "56.326900", "44.005900"),
            arrayOf("Челябинск", "Крупный промышленный центр.", "55.164400", "61.436800"),
            arrayOf("Самара", "Красивый город на Волге.", "53.241500", "50.221200"),
            arrayOf("Омск", "Один из крупнейших городов в Сибири.", "54.988500", "73.324200"),
            arrayOf("Ростов-на-Дону", "Южная столица России.", "47.235700", "39.701500"),
            arrayOf("Уфа", "Столица Республики Башкортостан.", "54.738800", "55.972100"),
            arrayOf("Красноярск", "Город на Енисее.", "56.018400", "92.867200"),
            arrayOf("Воронеж", "Колыбель русского флота.", "51.672000", "39.184300"),
            arrayOf("Пермь", "Крупный город на Каме.", "58.010500", "56.250200"),
            arrayOf("Волгоград", "Город-герой на Волге.", "48.708000", "44.513300"),
            arrayOf("Краснодар", "Южный центр и столица Кубани.", "45.035500", "38.975300"),
            arrayOf("Саратов", "Культурный и промышленный центр.", "51.533200", "46.034000"),
            arrayOf("Тюмень", "Первый русский город в Сибири.", "57.152200", "65.527200"),
            arrayOf("Сочи", "Главный курорт страны на Черном море.", "43.585500", "39.723300"),
            arrayOf("Владивосток", "Главный порт Тихого океана.", "43.115500", "131.885000"),
            arrayOf("Ижевск", "Оружейная столица России.", "56.852700", "53.211400"),
            arrayOf("Ярославль", "Жемчужина Золотого кольца.", "57.626100", "39.884500")
        )

        for (city in allCities) {
            val values = ContentValues().apply {
                put(COLUMN_NAME, city[0])
                put(COLUMN_DESC, city[1])
                put(COLUMN_IMAGE_RES, R.mipmap.ic_launcher)
                put(COLUMN_IMAGE_URI, null as String?)
                put(COLUMN_LAT, city[2].toDouble())
                put(COLUMN_LON, city[3].toDouble())
            }
            db?.insert(TABLE_NAME, null, values)
        }
    }

    fun getAllCities(): MutableList<City> {
        val cityList = mutableListOf<City>()
        val db = this.readableDatabase

        val cursorCheck = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        cursorCheck.moveToFirst()
        val count = cursorCheck.getInt(0)
        cursorCheck.close()

        if (count == 0) {
            db.close()
            populateAllCities(this.writableDatabase)
            return getAllCities()
        }

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
        val values = ContentValues().apply { put(COLUMN_DESC, newDesc) }
        db.update(TABLE_NAME, values, "$COLUMN_NAME = ?", arrayOf(cityName))
        db.close()
    }

    fun updateCityPhoto(cityName: String, newUri: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put(COLUMN_IMAGE_URI, newUri) }
        db.update(TABLE_NAME, values, "$COLUMN_NAME = ?", arrayOf(cityName))
        db.close()
    }
}