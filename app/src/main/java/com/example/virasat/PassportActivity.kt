package com.example.virasat

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.virasat.data.AppDatabase
import kotlinx.coroutines.launch

class PassportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F3E2C7"))
            clipToPadding = false
            setPadding(0, 0, 0, dp(32))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#6E1F14"))
            setPadding(dp(20), dp(18), dp(20), dp(24))
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = getString(R.string.travel_passport)
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F9E7B7"))
            setPadding(dp(8), 0, 0, 0)
        }

        topRow.addView(backButton, LinearLayout.LayoutParams(dp(48), dp(48)))
        topRow.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val subtitle = TextView(this).apply {
            text = getString(R.string.passport_subtitle)
            textSize = 15f
            setTextColor(Color.parseColor("#FFF8EC"))
            setPadding(0, dp(8), 0, 0)
        }

        header.addView(topRow)
        header.addView(subtitle)
        root.addView(header)

        val countCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(22), dp(20), dp(22))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FFF8EC"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.parseColor("#C49A35"))
            }
            elevation = 4f
        }

        val countText = TextView(this).apply {
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
            gravity = Gravity.CENTER
        }

        val badgeText = TextView(this).apply {
            text = getString(R.string.passport_badge_title)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#4E342E"))
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
        }

        countCard.addView(countText)
        countCard.addView(badgeText)

        root.addView(
            countCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(24), dp(22), dp(24), dp(12))
            }
        )

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(40))
        }

        root.addView(listContainer)

        scrollView.addView(root)
        setContentView(scrollView)

        lifecycleScope.launch {
            val visits = db.checkInDao().getAllCheckIns()

            countText.text = getString(R.string.visited_count, visits.size)

            listContainer.removeAllViews()

            if (visits.isEmpty()) {
                listContainer.addView(createEmptyPassportCard())
            } else {
                visits.forEach { visit ->
                    listContainer.addView(
                        createPassportStampCard(
                            siteName = visit.siteName,
                            checkedInAt = visit.checkedInAt
                        )
                    )
                }
            }
        }
    }

    private fun createEmptyPassportCard(): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(22))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FFF8EC"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.parseColor("#D7B377"))
            }
        }

        val title = TextView(this).apply {
            text = getString(R.string.passport_empty_title)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
        }

        val message = TextView(this).apply {
            text = getString(R.string.passport_empty_message)
            textSize = 15f
            setTextColor(Color.parseColor("#231815"))
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(4f, 1.0f)
        }

        card.addView(title)
        card.addView(message)

        return card
    }

    private fun createPassportStampCard(
        siteName: String,
        checkedInAt: Long
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FFF8EC"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), Color.parseColor("#C49A35"))
            }
            elevation = 3f
        }

        val stamp = TextView(this).apply {
            text = getString(R.string.passport_stamp)
            textSize = 12f
            letterSpacing = 0.12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#C49A35"))
        }

        val title = TextView(this).apply {
            text = siteName
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
            setPadding(0, dp(8), 0, dp(4))
        }

        val status = TextView(this).apply {
            text = getString(R.string.visited_status)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#4E342E"))
        }

        card.addView(stamp)
        card.addView(title)
        card.addView(status)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, dp(14))
        }

        card.layoutParams = params

        return card
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
