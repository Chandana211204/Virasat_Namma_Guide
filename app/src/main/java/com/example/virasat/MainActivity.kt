package com.example.virasat

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.virasat.data.HeritageRepository
import com.example.virasat.data.HeritageSite
import com.example.virasat.offline.OfflineMapCache
import com.example.virasat.util.ConnectivityUtils
import com.example.virasat.util.LanguageManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import android.graphics.drawable.GradientDrawable


class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SITE_ID = "siteId"
        const val EXTRA_HIDDEN_FACT_UNLOCKED = "hiddenFactUnlocked"
    }

    private val simulatedUserLatitude = 12.9716
    private val simulatedUserLongitude = 77.5946
    private var selectedRadiusKm = 250

    private lateinit var rootLayout: LinearLayout
    private lateinit var siteListContainer: LinearLayout
    private lateinit var radiusInfoText: TextView

    private lateinit var radius100Button: Button
    private lateinit var radius250Button: Button
    private lateinit var radius500Button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.applySavedLanguage(this)
        OfflineMapCache.refreshCache(this)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F3E2C7"))
            clipToPadding = false
            setPadding(0, 0, 0, dp(32))
        }

        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        createHeader()
        createActionButtons()
        createDiscoverySection()

        siteListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        rootLayout.addView(siteListContainer)

        scrollView.addView(rootLayout)
        setContentView(scrollView)

        showSitesForRadius(selectedRadiusKm)
    }

    private fun createHeader() {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(34), dp(24), dp(24))
            setBackgroundColor(Color.parseColor("#6E1F14"))
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 26f
            setTextColor(Color.parseColor("#F9E7B7"))
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "Discover Karnataka's hidden heritage"
            textSize = 14f
            setTextColor(Color.parseColor("#FFF8EC"))
            setPadding(0, dp(6), 0, 0)
        }

        header.addView(title)
        header.addView(subtitle)
        header.addView(createLanguageSwitcher())
        rootLayout.addView(header)
    }

    private fun createLanguageSwitcher(): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(14), 0, 0)
        }

        row.addView(
            createLanguageButton(getString(R.string.language_english), LanguageManager.ENGLISH),
            LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                setMargins(0, 0, dp(6), 0)
            }
        )
        row.addView(
            createLanguageButton(getString(R.string.language_kannada), LanguageManager.KANNADA),
            LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                setMargins(dp(6), 0, 0, 0)
            }
        )

        return row
    }

    private fun createLanguageButton(label: String, language: String): Button {
        val selected = LanguageManager.currentLanguage(this) == language
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (selected) Color.WHITE else Color.parseColor("#6E1F14"))
            background = GradientDrawable().apply {
                setColor(if (selected) Color.parseColor("#C49A35") else Color.parseColor("#FFF8EC"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.parseColor("#D7B377"))
            }
            setOnClickListener {
                LanguageManager.setLanguage(this@MainActivity, language)
                recreate()
            }
        }
    }

    private fun createActionButtons() {
        val actionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(24), dp(20), dp(24), dp(10))
        }

        val scanButton = Button(this).apply {
            text = getString(R.string.scan_qr)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.brown_button_bg)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, ScannerActivity::class.java))
            }
        }

        val passportButton = Button(this).apply {
            text = getString(R.string.travel_passport)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.brown_button_bg)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, PassportActivity::class.java))
            }
        }

        val buttonParams = LinearLayout.LayoutParams(
            0,
            dp(54),
            1f
        ).apply {
            setMargins(dp(4), 0, dp(4), 0)
        }

        actionLayout.addView(scanButton, buttonParams)
        actionLayout.addView(passportButton, buttonParams)

        rootLayout.addView(actionLayout)
    }

    private fun createDiscoverySection() {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(10), dp(24), dp(8))
        }

        val title = TextView(this).apply {
            text = "Nearby Heritage Sites"
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#231815"))
        }

        val simulatedLocation = TextView(this).apply {
            text = "Simulated location: Bengaluru, Karnataka"
            textSize = 13f
            setTextColor(Color.parseColor("#4E342E"))
            setPadding(0, dp(4), 0, dp(10))
        }

        radiusInfoText = TextView(this).apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
            setPadding(0, 0, 0, dp(10))
        }

        val radiusButtons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        radius100Button = createRadiusButton("100 km", 100)
        radius250Button = createRadiusButton("250 km", 250)
        radius500Button = createRadiusButton("500 km", 500)

        val radiusParams = LinearLayout.LayoutParams(
            0,
            dp(48),
            1f
        ).apply {
            setMargins(dp(3), 0, dp(3), 0)
        }

        radiusButtons.addView(radius100Button, radiusParams)
        radiusButtons.addView(radius250Button, radiusParams)
        radiusButtons.addView(radius500Button, radiusParams)


        section.addView(title)
        section.addView(simulatedLocation)
        section.addView(radiusInfoText)
        section.addView(radiusButtons)

        rootLayout.addView(section)
        updateRadiusButtonStyles()

    }

    private fun createRadiusButton(label: String, radiusKm: Int): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false

            setOnClickListener {
                selectedRadiusKm = radiusKm
                updateRadiusButtonStyles()
                animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(80)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start()
                    }
                    .start()

                showSitesForRadius(radiusKm)
            }
        }
    }
    private fun updateRadiusButtonStyles() {
        styleRadiusButton(radius100Button, selectedRadiusKm == 100)
        styleRadiusButton(radius250Button, selectedRadiusKm == 250)
        styleRadiusButton(radius500Button, selectedRadiusKm == 500)
    }

    private fun styleRadiusButton(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setTextColor(Color.WHITE)
            button.background = GradientDrawable().apply {
                setColor(Color.parseColor("#6E1F14"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), Color.parseColor("#C49A35"))
            }
            button.elevation = dp(5).toFloat()
        } else {
            button.setTextColor(Color.parseColor("#6E1F14"))
            button.background = GradientDrawable().apply {
                setColor(Color.parseColor("#FFF8EC"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.parseColor("#D7B377"))
            }
            button.elevation = dp(1).toFloat()
        }
    }




    private fun showSitesForRadius(radiusKm: Int) {
        siteListContainer.removeAllViews()

        val sitesWithDistance = HeritageRepository.sites
            .map { site ->
                site to calculateDistanceKm(
                    simulatedUserLatitude,
                    simulatedUserLongitude,
                    site.latitude,
                    site.longitude
                )
            }
            .filter { (_, distance) ->
                distance <= radiusKm
            }
            .sortedBy { (_, distance) ->
                distance
            }

        val offlinePrefix = if (ConnectivityUtils.isOnline(this)) "" else "${getString(R.string.offline_mode)} - "
        radiusInfoText.text = "${offlinePrefix}Showing sites within $radiusKm km - ${sitesWithDistance.size} found"

        if (sitesWithDistance.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No heritage sites found in this radius. Try 250 km or 500 km."
                textSize = 15f
                setTextColor(Color.parseColor("#231815"))
                setPadding(dp(24), dp(20), dp(24), dp(20))
            }

            siteListContainer.addView(emptyText)
            return
        }

        sitesWithDistance.forEach { (site, distance) ->
            siteListContainer.addView(createSiteCard(site, distance))
        }
    }

    private fun createSiteCard(site: HeritageSite, distanceKm: Double): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(18))
            setBackgroundColor(Color.parseColor("#FFF8EC"))
            elevation = 4f
            setOnClickListener {
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra(EXTRA_SITE_ID, site.id)
                intent.putExtra(EXTRA_HIDDEN_FACT_UNLOCKED, false)
                startActivity(intent)
            }
        }

        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(24), dp(12), dp(24), dp(16))
        }

        card.layoutParams = cardParams

        val image = ImageView(this).apply {
            setImageResource(site.imageRes)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        card.addView(
            image,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(150)
            )
        )

        val name = TextView(this).apply {
            text = site.displayName(LanguageManager.isKannada(this@MainActivity))
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
            setPadding(dp(18), dp(14), dp(18), dp(2))
        }

        val location = TextView(this).apply {
            text = site.displayLocation(LanguageManager.isKannada(this@MainActivity))
            textSize = 13f
            setTextColor(Color.parseColor("#4E342E"))
            setPadding(dp(18), 0, dp(18), dp(4))
        }

        val distance = TextView(this).apply {
            text = "Approx. ${distanceKm.toInt()} km away"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#C49A35"))
            setPadding(dp(18), 0, dp(18), dp(6))
        }

        val hint = TextView(this).apply {
            text = "Tap to explore history, legends and hidden facts"
            textSize = 12f
            setTextColor(Color.parseColor("#7A5C45"))
            gravity = Gravity.START
            setPadding(dp(18), 0, dp(18), 0)
        }

        card.addView(name)
        card.addView(location)
        card.addView(distance)
        card.addView(hint)

        return card
    }

    private fun calculateDistanceKm(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(endLat - startLat)
        val dLon = Math.toRadians(endLon - startLon)

        val lat1 = Math.toRadians(startLat)
        val lat2 = Math.toRadians(endLat)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
