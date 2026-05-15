package com.example.virasat

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.virasat.data.AppDatabase
import com.example.virasat.data.CheckInEntity
import com.example.virasat.data.HeritageRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import com.example.virasat.ar.ARHeritageActivity
import com.example.virasat.offline.OfflineMapActivity
import com.example.virasat.offline.OfflineMapCache
import com.example.virasat.util.ConnectivityUtils
import com.example.virasat.util.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL



class DetailActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var db: AppDatabase
    private data class WeatherInfo(
        val temperature: Double,
        val apparentTemperature: Double,
        val windSpeed: Double,
        val weatherCode: Int
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getDatabase(this)
        LanguageManager.applySavedLanguage(this)
        OfflineMapCache.refreshCache(this)

        val siteId = intent.getStringExtra(MainActivity.EXTRA_SITE_ID) ?: return
        val site = HeritageRepository.getSiteById(siteId) ?: return
        val useKannada = LanguageManager.isKannada(this)
        val siteName = site.displayName(useKannada)
        val siteLocation = site.displayLocation(useKannada)
        val historyTextValue = site.displayHistory(useKannada)
        val architectureTextValue = site.displayArchitecture(useKannada)
        val legendTextValue = site.displayLegend(useKannada)
        val hiddenFactTextValue = site.displayHiddenFact(useKannada)


        val hiddenFactUnlocked = intent.getBooleanExtra(
            MainActivity.EXTRA_HIDDEN_FACT_UNLOCKED,
            false
        )

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F3E2C7"))
            isFillViewport = false
            clipToPadding = false
            setPadding(0, 0, 0, dp(28))
        }


        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#6E1F14"))
            setPadding(12, 10, 18, 10)
        }

        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener { finish() }
        }

        val topTitle = TextView(this).apply {
            text = siteName
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F9E7B7"))
            setPadding(12, 0, 0, 0)
        }

        topBar.addView(backButton, LinearLayout.LayoutParams(dp(48), dp(48)))

        topBar.addView(
            topTitle,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        rootLayout.addView(topBar)

        val image = ImageView(this).apply {
            setImageResource(site.imageRes)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        rootLayout.addView(
            image,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(230)
            )
        )

        val titleBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#6E1F14"))
            setPadding(28, 24, 28, 24)
        }

        val title = TextView(this).apply {
            text = siteName
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F9E7B7"))
        }

        val location = TextView(this).apply {
            text = siteLocation
            textSize = 15f
            setTextColor(Color.parseColor("#FFF8EC"))
            setPadding(0, 8, 0, 0)
        }

        titleBlock.addView(title)
        titleBlock.addView(location)
        rootLayout.addView(titleBlock)
        val weatherText = TextView(this).apply {
            text = getString(R.string.weather_loading)
            textSize = 15f
            setTextColor(Color.parseColor("#231815"))
            setPadding(0, 8, 0, 0)
            setLineSpacing(4f, 1.0f)
        }

        rootLayout.addView(createWeatherCard(weatherText))

        lifecycleScope.launch {
            try {
                val weatherInfo = fetchWeatherInfo(site.latitude, site.longitude)

                weatherText.text = buildString {
                    append("Temperature: ${weatherInfo.temperature}°C\n")
                    append("Feels like: ${weatherInfo.apparentTemperature}°C\n")
                    append("Wind: ${weatherInfo.windSpeed} km/h\n")
                    append("Condition: ${getWeatherDescription(weatherInfo.weatherCode)}")
                }
            } catch (exception: Exception) {
                weatherText.text = getString(R.string.weather_unavailable)
            }
        }


        rootLayout.addView(createInfoCard(getString(R.string.history_label), historyTextValue))
        rootLayout.addView(createInfoCard(getString(R.string.architecture_label), architectureTextValue))
        rootLayout.addView(createInfoCard(getString(R.string.legend_label), legendTextValue))

        rootLayout.addView(
            createHiddenFactCard(
                siteId = site.id,
                hiddenFact = hiddenFactTextValue,
                hiddenFactUnlocked = hiddenFactUnlocked
            )
        )

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 18, 24, dp(90))
        }

        val mapsButton = Button(this).apply {
            text = getString(R.string.open_route_maps)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.maroon_button_bg)

            setOnClickListener {
                openGoogleMapsRoute(
                    siteId = site.id,
                    latitude = site.latitude,
                    longitude = site.longitude
                )
            }
        }
        val storyButton = Button(this).apply {
            text = getString(R.string.tell_me_story)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.maroon_button_bg)

            setOnClickListener {
                val intent = Intent(this@DetailActivity, StoryActivity::class.java)
                intent.putExtra(StoryActivity.EXTRA_SITE_ID, site.id)
                startActivity(intent)
            }
        }

        val arButton = Button(this).apply {
            text = getString(R.string.open_ar_explore)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.maroon_button_bg)

            setOnClickListener {
                val intent = Intent(this@DetailActivity, ARHeritageActivity::class.java)
                intent.putExtra(ARHeritageActivity.EXTRA_SITE_ID, site.id)
                startActivity(intent)
            }
        }

        val offlineButton = Button(this).apply {
            text = getString(R.string.view_offline_location)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.brown_button_bg)

            setOnClickListener {
                val intent = Intent(this@DetailActivity, OfflineMapActivity::class.java)
                intent.putExtra(OfflineMapActivity.EXTRA_SITE_ID, site.id)
                startActivity(intent)
            }
        }


        val audioButton = Button(this).apply {
            text = getString(R.string.play_audio_guide)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.brown_button_bg)

            setOnClickListener {
                if (site.audioRes == 0) {
                    Toast.makeText(
                        this@DetailActivity,
                        getString(R.string.audio_coming_soon),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this@DetailActivity, site.audioRes)
                    mediaPlayer?.start()
                    text = getString(R.string.pause_audio_guide)
                } else if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    text = getString(R.string.play_audio_guide)
                } else {
                    mediaPlayer?.start()
                    text = getString(R.string.pause_audio_guide)
                }
            }
        }

        val checkInButton = Button(this).apply {
            text = getString(R.string.check_in)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.maroon_button_bg)

            setOnClickListener {
                lifecycleScope.launch {
                    db.checkInDao().checkIn(
                        CheckInEntity(
                            siteId = site.id,
                            siteName = site.name,
                            checkedInAt = System.currentTimeMillis()
                        )
                    )

                    text = getString(R.string.visited_status)
                    isEnabled = false

                    Toast.makeText(
                        this@DetailActivity,
                        getString(R.string.checked_in),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        lifecycleScope.launch {
            val alreadyCheckedIn = db.checkInDao().isCheckedIn(site.id)

            if (alreadyCheckedIn) {
                checkInButton.text = getString(R.string.visited_status)
                checkInButton.isEnabled = false
            }
        }



        val buttonParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(54)
        ).apply {
            setMargins(0, 0, 0, 14)
        }

        buttonLayout.addView(storyButton, buttonParams)
        buttonLayout.addView(arButton, buttonParams)
        buttonLayout.addView(mapsButton, buttonParams)
        buttonLayout.addView(offlineButton, buttonParams)
        buttonLayout.addView(audioButton, buttonParams)
        buttonLayout.addView(checkInButton, buttonParams)



        rootLayout.addView(buttonLayout)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun createInfoCard(heading: String, body: String): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 20, 22, 22)
            setBackgroundResource(R.drawable.info_card_bg)
            elevation = 3f
        }

        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(24, 16, 24, 0)
        }

        card.layoutParams = cardParams

        val title = TextView(this).apply {
            text = heading
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
        }

        val content = TextView(this).apply {
            text = body
            textSize = 15f
            setTextColor(Color.parseColor("#231815"))
            setPadding(0, 8, 0, 0)
            setLineSpacing(4f, 1.0f)
        }

        card.addView(title)
        card.addView(content)

        return card
    }

    private fun createHiddenFactCard(
        siteId: String,
        hiddenFact: String,
        hiddenFactUnlocked: Boolean
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 20, 22, 22)
            setBackgroundResource(R.drawable.info_card_bg)
            elevation = 3f
        }

        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(24, 16, 24, 0)
        }

        card.layoutParams = cardParams

        val title = TextView(this).apply {
            text = getString(R.string.hidden_fact_label)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
        }

        val content = TextView(this).apply {
            text = if (hiddenFactUnlocked) {
                hiddenFact
            } else {
                getString(R.string.hidden_fact_locked)
            }
            textSize = 15f
            setTextColor(Color.parseColor("#231815"))
            setPadding(0, 8, 0, 0)
            setLineSpacing(4f, 1.0f)
        }

        val qrPayload = "virasat://hiddenfact?siteId=$siteId"

        val qrImage = ImageView(this).apply {
            setImageBitmap(generateQrBitmap(qrPayload, dp(210)))
            setBackgroundColor(Color.WHITE)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            adjustViewBounds = true
            visibility = View.GONE
        }


        if (!hiddenFactUnlocked) {
            card.setOnClickListener {
                qrImage.visibility = if (qrImage.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }

        card.addView(title)
        card.addView(content)

        if (!hiddenFactUnlocked) {
            card.addView(
                qrImage,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(260)
                ).apply {
                    setMargins(0, dp(16), 0, dp(12))
                }
            )

        }

        return card
    }
    private fun createWeatherCard(weatherText: TextView): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 20, 22, 22)
            setBackgroundResource(R.drawable.info_card_bg)
            elevation = 3f
        }

        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(24, 16, 24, 0)
        }

        card.layoutParams = cardParams

        val title = TextView(this).apply {
            text = getString(R.string.weather_title)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
        }

        card.addView(title)
        card.addView(weatherText)

        return card
    }


    private fun generateQrBitmap(text: String, size: Int): Bitmap {
        val bitMatrix = QRCodeWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            size,
            size
        )

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }

        return bitmap
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
    private fun openGoogleMapsRoute(siteId: String, latitude: Double, longitude: Double) {
        if (!ConnectivityUtils.isOnline(this)) {
            val intent = Intent(this, OfflineMapActivity::class.java)
            intent.putExtra(OfflineMapActivity.EXTRA_SITE_ID, siteId)
            startActivity(intent)
            return
        }

        val mapsUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")

        val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(mapsIntent)
        } catch (exception: Exception) {
            val browserUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=driving"
            )

            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            startActivity(browserIntent)
        }
    }
    private suspend fun fetchWeatherInfo(
        latitude: Double,
        longitude: Double
    ): WeatherInfo {
        return withContext(Dispatchers.IO) {
            val urlText =
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$latitude" +
                        "&longitude=$longitude" +
                        "&current=temperature_2m,apparent_temperature,weather_code,wind_speed_10m" +
                        "&timezone=auto"

            val connection = URL(urlText).openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"

            val response = connection.inputStream.bufferedReader().use {
                it.readText()
            }

            connection.disconnect()

            val json = JSONObject(response)
            val current = json.getJSONObject("current")

            WeatherInfo(
                temperature = current.getDouble("temperature_2m"),
                apparentTemperature = current.getDouble("apparent_temperature"),
                windSpeed = current.getDouble("wind_speed_10m"),
                weatherCode = current.getInt("weather_code")
            )
        }
    }
    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Light drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            80, 81, 82 -> "Rain showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Weather condition available"
        }
    }




    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
