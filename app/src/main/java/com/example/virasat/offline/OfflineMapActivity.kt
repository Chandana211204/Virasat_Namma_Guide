package com.example.virasat.offline

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.virasat.R
import com.example.virasat.util.ConnectivityUtils

class OfflineMapActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SITE_ID = "offlineSiteId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OfflineMapCache.refreshCache(this)

        val siteId = intent.getStringExtra(EXTRA_SITE_ID)
        val locations = if (siteId != null) {
            listOfNotNull(OfflineMapCache.getCachedSite(this, siteId))
        } else {
            OfflineMapCache.getCachedSites(this)
        }

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F3E2C7"))
            clipToPadding = false
            setPadding(0, 0, 0, dp(32))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(32))
        }

        val mode = if (ConnectivityUtils.isOnline(this)) {
            getString(R.string.online_mode)
        } else {
            getString(R.string.offline_mode)
        }

        root.addView(titleText(getString(R.string.offline_map_title)))
        root.addView(bodyText("$mode\n${getString(R.string.offline_banner)}"))

        locations.forEach { location ->
            root.addView(createLocationCard(location))
        }

        scrollView.addView(root)
        setContentView(scrollView)
    }

    private fun createLocationCard(location: CachedHeritageLocation): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundResource(R.drawable.info_card_bg)
            elevation = 4f
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp(16), 0, 0)
        }
        card.layoutParams = params

        card.addView(titleText(location.name))
        card.addView(bodyText(location.location))
        card.addView(
            bodyText(
                getString(
                    R.string.offline_coordinates,
                    location.latitude,
                    location.longitude
                )
            )
        )
        card.addView(bodyText(getString(R.string.offline_route_note)))

        val retryButton = Button(this).apply {
            text = getString(R.string.retry_connection)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.brown_button_bg)
            setOnClickListener {
                if (ConnectivityUtils.isOnline(this@OfflineMapActivity)) {
                    Toast.makeText(
                        this@OfflineMapActivity,
                        getString(R.string.connection_available),
                        Toast.LENGTH_SHORT
                    ).show()
                    openGoogleMaps(location.latitude, location.longitude)
                } else {
                    Toast.makeText(
                        this@OfflineMapActivity,
                        getString(R.string.connection_unavailable),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        card.addView(
            retryButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                setMargins(0, dp(14), 0, 0)
            }
        )

        return card
    }

    private fun openGoogleMaps(latitude: Double, longitude: Double) {
        val browserUri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=driving"
        )
        startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }

    private fun titleText(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
        }
    }

    private fun bodyText(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 15f
            setTextColor(Color.parseColor("#231815"))
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(4f, 1.0f)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
