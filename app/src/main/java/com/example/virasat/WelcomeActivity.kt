package com.example.virasat

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)

        val backgroundImage = ImageView(this).apply {
            setImageResource(R.drawable.welcome_hero)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        root.addView(
            backgroundImage,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val darkOverlay = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#66000000"),
                    Color.parseColor("#33000000"),
                    Color.parseColor("#DD000000")
                )
            )
        }

        root.addView(
            darkOverlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(42))
        }

        val titlePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(22), dp(24), dp(22), dp(22))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#B36E1F14"))
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), Color.parseColor("#C49A35"))
            }
        }

        val eyebrow = TextView(this).apply {
            text = "KARNATAKA HERITAGE GUIDE"
            textSize = 12f
            letterSpacing = 0.12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F9E7B7"))
            gravity = Gravity.CENTER
        }

        val titleTop = TextView(this).apply {
            text = "Virasat"
            textSize = 42f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setShadowLayer(5f, 0f, 3f, Color.BLACK)
            setPadding(0, dp(8), 0, 0)
        }

        val goldLine = TextView(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#C49A35"))
                cornerRadius = dp(2).toFloat()
            }
        }

        val titleBottom = TextView(this).apply {
            text = "Namma Guide"
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setTextColor(Color.parseColor("#F9E7B7"))
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(10))
        }

        val subtitle = TextView(this).apply {
            text = "Discover hidden temples, inscriptions, legends and stories from Karnataka's living heritage."
            textSize = 15f
            setTextColor(Color.parseColor("#FFF8EC"))
            gravity = Gravity.CENTER
            setLineSpacing(5f, 1.0f)
        }

        val exploreButton = Button(this).apply {
            text = "EXPLORE HERITAGE"
            textSize = 14f
            letterSpacing = 0.08f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.parseColor("#7B241C"),
                    Color.parseColor("#4E342E")
                )
            ).apply {
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.parseColor("#C49A35"))
            }

            setOnClickListener {
                val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        titlePanel.addView(eyebrow)
        titlePanel.addView(titleTop)

        titlePanel.addView(
            goldLine,
            LinearLayout.LayoutParams(dp(105), dp(4)).apply {
                setMargins(0, dp(4), 0, dp(4))
            }
        )

        titlePanel.addView(titleBottom)
        titlePanel.addView(subtitle)

        contentLayout.addView(
            titlePanel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        contentLayout.addView(
            exploreButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {
                setMargins(0, dp(18), 0, 0)
            }
        )

        root.addView(
            contentLayout,
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
