package com.example.virasat

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.virasat.data.HeritageRepository
import com.example.virasat.util.LanguageManager
import java.util.Locale

class StoryActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        const val EXTRA_SITE_ID = "storySiteId"
    }

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var isSpeaking = false
    private lateinit var storyButton: Button
    private var storyToRead: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.applySavedLanguage(this)
        textToSpeech = TextToSpeech(this, this)

        val siteId = intent.getStringExtra(EXTRA_SITE_ID) ?: return
        val site = HeritageRepository.getSiteById(siteId) ?: return
        val useKannada = LanguageManager.isKannada(this)

        storyToRead = site.displayStory(useKannada)

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

        root.addView(
            FrameLayout(this).apply {
                setBackgroundColor(Color.parseColor("#B3000000"))
            },
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val scrollView = ScrollView(this).apply {
            clipToPadding = false
            setPadding(0, 0, 0, dp(32))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(32), dp(24), dp(42))
        }

        val backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener { finish() }
        }

        val label = TextView(this).apply {
            text = getString(R.string.ai_story_label)
            textSize = 12f
            letterSpacing = 0.12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F9E7B7"))
            setPadding(0, dp(24), 0, dp(6))
        }

        val title = TextView(this).apply {
            text = site.displayName(useKannada)
            textSize = 31f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setShadowLayer(5f, 0f, 3f, Color.BLACK)
        }

        val location = TextView(this).apply {
            text = site.displayLocation(useKannada)
            textSize = 15f
            setTextColor(Color.parseColor("#FFF8EC"))
            setPadding(0, dp(6), 0, dp(24))
        }

        storyButton = Button(this).apply {
            text = getString(R.string.listen_story)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.maroon_button_bg)
            setOnClickListener { toggleStoryAudio() }
        }

        val storyCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(26), dp(24), dp(26))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EFFFF8EC"))
                cornerRadius = dp(18).toFloat()
                setStroke(dp(2), Color.parseColor("#C49A35"))
            }
        }

        storyCard.addView(
            TextView(this).apply {
                text = storyToRead
                textSize = 17f
                setTextColor(Color.parseColor("#231815"))
                setLineSpacing(7f, 1.0f)
            }
        )

        content.addView(
            backButton,
            LinearLayout.LayoutParams(dp(56), dp(56)).apply {
                gravity = Gravity.START
            }
        )
        content.addView(label)
        content.addView(title)
        content.addView(location)
        content.addView(
            storyButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                setMargins(0, 0, 0, dp(18))
            }
        )
        content.addView(
            storyCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        scrollView.addView(content)
        root.addView(
            scrollView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = if (LanguageManager.isKannada(this)) Locale("kn", "IN") else Locale.ENGLISH
            val result = textToSpeech?.setLanguage(locale)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            textToSpeech?.setSpeechRate(0.88f)
            textToSpeech?.setPitch(1.0f)
        }
    }

    private fun toggleStoryAudio() {
        if (!isTtsReady) {
            Toast.makeText(this, getString(R.string.tts_not_available), Toast.LENGTH_SHORT).show()
            return
        }

        if (isSpeaking) {
            textToSpeech?.stop()
            isSpeaking = false
            storyButton.text = getString(R.string.listen_story)
        } else {
            textToSpeech?.speak(storyToRead, TextToSpeech.QUEUE_FLUSH, null, "story_audio")
            isSpeaking = true
            storyButton.text = getString(R.string.stop_story)
        }
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
