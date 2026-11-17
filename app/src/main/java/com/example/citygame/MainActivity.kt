package com.example.citygame

import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.xml.KonfettiView

class MainActivity : AppCompatActivity() {

    private val historyList = mutableListOf<String>()
    private val usedCities = mutableSetOf<String>()
    private lateinit var adapter: CityAdapter
    private lateinit var recyclerView: RecyclerView
    private var requiredLetter: Char? = null

    private val computerCities = mutableSetOf<String>()
    private lateinit var dbHelper: CityDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        requestNotificationPermission()

        dbHelper = CityDatabaseHelper(this)
        loadCitiesFromAssetsAndDb()

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
            val scaleDown = ObjectAnimator.ofFloat(buttonSend, "scaleX", 1f, 0.9f).apply { duration = 100 }
            val scaleUp = ObjectAnimator.ofFloat(buttonSend, "scaleX", 0.9f, 1f).apply { duration = 100 }
            AnimatorSet().apply { playSequentially(scaleDown, scaleUp) }.start()

            val userCity = editTextCity.text.toString().trim()

            if (userCity.isEmpty()) {
                showAlert("Ошибка", "Введите название города!")
                return@setOnClickListener
            }

            val normalizedUserCity = userCity.lowercase()

            if (!isValidCity(normalizedUserCity)) {
                showAlert("Неизвестный город", "Такого города нет в нашей базе данных или справочнике!")
                return@setOnClickListener
            }

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
            adapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(historyList.size - 1)
            editTextCity.text.clear()

            val typingMessage = "Бот печатает"
            historyList.add(typingMessage)
            val typingIndex = historyList.size - 1
            adapter.notifyItemInserted(typingIndex)
            recyclerView.scrollToPosition(typingIndex)

            val handler = Handler(Looper.getMainLooper())
            val runnable = createTypingAnimation(handler, typingIndex)
            handler.postDelayed(runnable, 350)

            handler.postDelayed({
                handler.removeCallbacks(runnable)

                val index = historyList.indexOfFirst { it.startsWith("Бот печатает") }
                if (index != -1) {
                    historyList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }

                val botCity = computerCities.firstOrNull { city ->
                    val norm = city.lowercase()
                    norm.first() == lastUserLetter && !usedCities.contains(norm)
                }

                if (botCity != null) {
                    usedCities.add(botCity.lowercase())
                    requiredLetter = getValidLastLetter(botCity.lowercase())
                    historyList.add(botCity)

                    val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
                    buttonSend.startAnimation(blinkAnimation)
                } else {
                    showVictorySequence(lastUserLetter)
                    requiredLetter = null
                }

                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(historyList.size - 1)
            }, 1500)
        }
    }

    private fun loadCitiesFromAssetsAndDb() {
        computerCities.clear()
        val dbCities = dbHelper.getAllCities()
        for (city in dbCities) {
            computerCities.add(city.name)
        }
    }

    private fun isValidCity(cityName: String): Boolean {
        val dbCities = dbHelper.getAllCities()
        for (city in dbCities) {
            computerCities.add(city.name)
        }
        return computerCities.any { it.equals(cityName, ignoreCase = true) }
    }

    private fun createTypingAnimation(handler: Handler, typingIndex: Int): Runnable {
        var dotCount = 0
        return object : Runnable {
            override fun run() {
                if (historyList.getOrNull(typingIndex)?.startsWith("Бот печатает") == true) {
                    dotCount = (dotCount % 3) + 1
                    val dots = ".".repeat(dotCount)
                    historyList[typingIndex] = "Бот печатает$dots"
                    adapter.notifyItemChanged(typingIndex)
                    handler.postDelayed(this, 350)
                }
            }
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
        val editText = findViewById<EditText>(R.id.editTextCity)
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
        editText.startAnimation(shakeAnimation)

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
            R.id.menu_check_notification -> {
                showNotificationDialog()
            }
            R.id.menu_about -> {
                val dialog = AlertDialog.Builder(this)
                try {
                    dialog.setMessage(
                        "Игра «Города» версия " +
                                packageManager.getPackageInfo(packageName, 0).versionName +
                                "\r\n\nПрограмма с примером выполнения диалогового окна" +
                                "\r\n\nАвтор - " + getString(R.string.fio)
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                dialog.setTitle("О программе")
                dialog.setNeutralButton("OK") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                dialog.setIcon(R.mipmap.ic_launcher_round)
                dialog.create().show()
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
            R.id.menu_cities -> {
                val intent = android.content.Intent(this, CitiesActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showVictorySequence(lastLetter: Char) {
        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            position = Position.Relative(0.5, 0.5),
            timeToLive = 2500L,
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
        )
        konfettiView.start(party)

        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.victory_sound)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        sendVictoryNotification()

        showAlert(
            "Победа!",
            "Компьютер не знает больше городов на букву «${lastLetter.uppercaseChar()}». Вы выиграли!"
        )
    }

    private fun showNotificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Проверить уведомление")
            .setMessage("Отправить тестовое уведомление через 5 секунд?\n\n")
            .setPositiveButton("Да") { _, _ ->
                android.widget.Toast.makeText(this, "Уведомление придет через 5 секунд...", android.widget.Toast.LENGTH_SHORT).show()


                Handler(Looper.getMainLooper()).postDelayed({
                    showTestNotification()
                }, 5000)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showTestNotification() {
        val builder = NotificationCompat.Builder(this, "GAME_CHANNEL_ID")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Привет из игры «Города»!")
            .setContentText("Это тестовое уведомление со звуком.")
            .setTicker("Новое уведомление!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(3, builder.build())
        }
    }

    private fun sendVictoryNotification() {
        val builder = NotificationCompat.Builder(this, "GAME_CHANNEL_ID")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Поздравляем с победой!")
            .setContentText("Компьютер сдался. Вы настоящий знаток городов!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setTicker("Новое уведомление!")
            .setAutoCancel(true)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Игровые события"
            val descriptionText = "Уведомления о победах в игре"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("GAME_CHANNEL_ID", name, importance).apply {
                description = descriptionText

                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val soundUri = android.net.Uri.parse(
                    android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                            packageName + "/" + R.raw.victory_sound
                )
                setSound(soundUri, audioAttributes)
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}