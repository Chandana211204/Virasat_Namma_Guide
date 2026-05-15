package com.example.virasat

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.virasat.data.HeritageRepository

class HiddenFactActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SITE_ID = "hiddenFactSiteId"
        const val EXTRA_HIDDEN_FACT = "hiddenFactText"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val siteIdFromLink = intent.data?.getQueryParameter("siteId")

        val siteId = siteIdFromLink
            ?: intent.getStringExtra(EXTRA_SITE_ID)
            ?: return

        val site = HeritageRepository.getSiteById(siteId) ?: return

        val hiddenFact = intent.getStringExtra(EXTRA_HIDDEN_FACT)
            ?: site.hiddenFact

        val root = FrameLayout(this)

        val backgroundImage = ImageView(this).apply {
            setImageResource(site.imageRes)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        root.addView(
            backgroundImage,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#AA000000"))
        }

        root.addView(
            overlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(26), dp(32), dp(26), dp(32))
        }

        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener { finish() }
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(28), dp(24), dp(28))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EFFFF8EC"))
                cornerRadius = dp(18).toFloat()
                setStroke(dp(2), Color.parseColor("#C49A35"))
            }
        }

        val label = TextView(this).apply {
            text = "UNLOCKED HIDDEN FACT"
            textSize = 12f
            letterSpacing = 0.12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#7B241C"))
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = site.name
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(16))
        }

        val factText = TextView(this).apply {
            text = hiddenFact
            textSize = 17f
            setTextColor(Color.parseColor("#231815"))
            gravity = Gravity.CENTER
            setLineSpacing(6f, 1.0f)
        }

        card.addView(label)
        card.addView(title)
        card.addView(factText)

        content.addView(
            backButton,
            LinearLayout.LayoutParams(dp(56), dp(56)).apply {
                gravity = Gravity.START
                setMargins(0, 0, 0, dp(24))
            }
        )

        content.addView(
            card,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            content,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
