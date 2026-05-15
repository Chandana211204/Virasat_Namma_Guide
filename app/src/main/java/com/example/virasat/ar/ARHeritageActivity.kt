package com.example.virasat.ar

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraState
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.virasat.R
import com.example.virasat.data.HeritageRepository
import com.example.virasat.data.HeritageSite
import com.example.virasat.util.LanguageManager
import com.google.ar.core.ArCoreApk

class ARHeritageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SITE_ID = "arSiteId"
        private const val TAG = "ARHeritageActivity"
    }

    private var cameraController: LifecycleCameraController? = null
    private var previewView: PreviewView? = null
    private var placedMarker: View? = null
    private var reticleView: View? = null
    private var guideView: TextView? = null
    private lateinit var root: FrameLayout
    private lateinit var site: HeritageSite
    private var userRequestedArInstall = true

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCameraPreview()
            } else {
                Log.w(TAG, "Camera permission denied. Showing non-camera AR fallback.")
                Toast.makeText(this, getString(R.string.ar_permission_needed), Toast.LENGTH_LONG).show()
                showFallbackPreview(getString(R.string.ar_camera_permission_fallback))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.applySavedLanguage(this)

        val siteId = intent.getStringExtra(EXTRA_SITE_ID)
        site = HeritageRepository.getSiteById(siteId.orEmpty()) ?: run {
            Toast.makeText(this, getString(R.string.ar_site_missing), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        root = FrameLayout(this)
        setContentView(root)

        val arMessage = prepareArCoreStatus()
        buildCameraUi(arMessage)
        requestCameraOrFallback()
    }

    override fun onResume() {
        super.onResume()
        userRequestedArInstall = false
    }

    override fun onDestroy() {
        cameraController?.unbind()
        cameraController = null
        super.onDestroy()
    }

    private fun prepareArCoreStatus(): String {
        return try {
            val installStatus = ArCoreApk.getInstance().requestInstall(this, userRequestedArInstall)
            if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                Log.i(TAG, "Requested Google Play Services for AR installation.")
                getString(R.string.ar_install_requested)
            } else {
                getAvailabilityMessage(ArCoreApk.getInstance().checkAvailability(this))
            }
        } catch (exception: Exception) {
            Log.w(TAG, "ARCore install/availability check failed.", exception)
            getString(R.string.ar_unavailable)
        }
    }

    private fun getAvailabilityMessage(availability: ArCoreApk.Availability): String {
        return when (availability) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> getString(R.string.ar_supported)
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> getString(R.string.ar_update_needed)
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> getString(R.string.ar_install_needed)
            ArCoreApk.Availability.UNKNOWN_CHECKING -> getString(R.string.ar_checking)
            ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> getString(R.string.ar_check_timeout)
            ArCoreApk.Availability.UNKNOWN_ERROR -> getString(R.string.ar_check_error)
            ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> getString(R.string.ar_device_not_supported)
        }
    }

    private fun buildCameraUi(arMessage: String) {
        root.removeAllViews()

        root.addView(
            ImageView(this).apply {
                setImageResource(site.imageRes)
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = 0.92f
            },
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val preview = PreviewView(this).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            setBackgroundColor(Color.TRANSPARENT)
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    placeHeritageMarker(event.x, event.y)
                    true
                } else {
                    true
                }
            }
        }
        previewView = preview

        root.addView(
            preview,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(createTopStatus())
        guideView = createPlacementGuide()
        reticleView = createCenterReticle()
        root.addView(guideView)
        root.addView(reticleView)
        root.addView(createFloatingInfoCard(arMessage))
    }

    private fun createTopStatus(): TextView {
        return TextView(this).apply {
            text = getString(R.string.ar_ready_short)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = roundedBackground("#8A231815", dp(18))
            setPadding(dp(14), dp(8), dp(14), dp(8))
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = statusBarHeight() + dp(10)
            }
        }
    }

    private fun createFloatingInfoCard(arMessage: String): View {
        val useKannada = LanguageManager.isKannada(this)

        val overlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(20))
            setBackgroundColor(Color.parseColor("#D96E1F14"))
        }

        overlay.addView(
            TextView(this).apply {
                text = getString(R.string.ar_title)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#F9E7B7"))
            }
        )

        overlay.addView(
            TextView(this).apply {
                text = site.displayName(useKannada)
                textSize = 21f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                setPadding(0, dp(6), 0, dp(4))
            }
        )

        overlay.addView(
            TextView(this).apply {
                text = site.displayHistory(useKannada)
                textSize = 13f
                setTextColor(Color.WHITE)
                setLineSpacing(4f, 1.0f)
                maxLines = 3
            }
        )

        return overlay.apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                setMargins(dp(16), 0, dp(16), dp(18))
            }
        }
    }

    private fun createPlacementGuide(): TextView {
        return TextView(this).apply {
            text = getString(R.string.ar_tap_to_place)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            gravity = Gravity.CENTER
            background = roundedBackground("#99231815", dp(22))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = statusBarHeight() + dp(58)
            }
        }
    }

    private fun createCenterReticle(): View {
        val reticle = FrameLayout(this)

        reticle.addView(
            View(this).apply { setBackgroundColor(Color.parseColor("#F9E7B7")) },
            FrameLayout.LayoutParams(dp(40), dp(2), Gravity.CENTER)
        )

        reticle.addView(
            View(this).apply { setBackgroundColor(Color.parseColor("#F9E7B7")) },
            FrameLayout.LayoutParams(dp(2), dp(40), Gravity.CENTER)
        )

        return reticle.apply {
            alpha = 0.85f
            layoutParams = FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER)
        }
    }

    private fun placeHeritageMarker(x: Float, y: Float) {
        placedMarker?.let { root.removeView(it) }

        val useKannada = LanguageManager.isKannada(this)
        val marker = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = roundedBackground("#F2F9E7B7", dp(14))
            elevation = dp(10).toFloat()
        }

        marker.addView(
            TextView(this).apply {
                text = getString(R.string.ar_marker_label)
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#6E1F14"))
                gravity = Gravity.CENTER
            }
        )

        marker.addView(
            TextView(this).apply {
                text = site.displayName(useKannada)
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#231815"))
                gravity = Gravity.CENTER
                maxLines = 2
            }
        )

        val markerWidth = dp(210)
        val markerHeight = dp(68)
        val left = (x.toInt() - markerWidth / 2).coerceIn(dp(8), resources.displayMetrics.widthPixels - markerWidth - dp(8))
        val top = (y.toInt() - markerHeight - dp(18)).coerceIn(statusBarHeight() + dp(106), resources.displayMetrics.heightPixels - markerHeight - dp(190))

        placedMarker = marker
        root.addView(
            marker,
            FrameLayout.LayoutParams(markerWidth, markerHeight).apply {
                leftMargin = left
                topMargin = top
            }
        )

        guideView?.text = getString(R.string.ar_marker_placed_hint)
        reticleView?.animate()?.alpha(0f)?.setDuration(180)?.start()
    }

    private fun requestCameraOrFallback() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Log.w(TAG, "Device reports no usable camera.")
            showFallbackPreview(getString(R.string.ar_no_camera))
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startCameraPreview()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCameraPreview() {
        Toast.makeText(this, getString(R.string.ar_open_camera), Toast.LENGTH_SHORT).show()

        try {
            val preview = previewView ?: return
            val controller = LifecycleCameraController(this).apply {
                cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            }

            preview.controller = controller
            controller.bindToLifecycle(this)

            controller.cameraInfo?.cameraState?.observe(this) { cameraState ->
                val error = cameraState.error
                if (error != null) {
                    Log.e(TAG, "CameraX state error: ${error.code}", error.cause)
                    showFallbackPreview(getString(R.string.ar_camera_failed))
                } else if (cameraState.type == CameraState.Type.OPEN) {
                    Log.i(TAG, "CameraX camera opened successfully.")
                }
            }

            cameraController = controller
        } catch (exception: IllegalArgumentException) {
            Log.e(TAG, "No compatible back camera for AR preview.", exception)
            showFallbackPreview(getString(R.string.ar_no_back_camera))
        } catch (exception: Exception) {
            Log.e(TAG, "Unexpected AR camera startup failure.", exception)
            showFallbackPreview(getString(R.string.ar_camera_failed))
        }
    }

    private fun showFallbackPreview(reason: String) {
        cameraController?.unbind()
        cameraController = null
        previewView = null
        root.removeAllViews()

        val useKannada = LanguageManager.isKannada(this)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F3E2C7"))
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(26), dp(24), dp(28))
        }

        content.addView(
            TextView(this).apply {
                text = getString(R.string.ar_fallback_title)
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#6E1F14"))
            }
        )

        content.addView(
            TextView(this).apply {
                text = reason
                textSize = 15f
                setTextColor(Color.parseColor("#3A241D"))
                setPadding(0, dp(8), 0, dp(16))
                setLineSpacing(5f, 1.0f)
            }
        )

        content.addView(
            ImageView(this).apply {
                setImageResource(site.imageRes)
                scaleType = ImageView.ScaleType.CENTER_CROP
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(220)
            )
        )

        content.addView(
            TextView(this).apply {
                text = site.displayName(useKannada)
                textSize = 26f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#6E1F14"))
                setPadding(0, dp(18), 0, dp(6))
            }
        )

        content.addView(
            TextView(this).apply {
                text = site.displayHistory(useKannada)
                textSize = 15f
                setTextColor(Color.parseColor("#231815"))
                setLineSpacing(5f, 1.0f)
            }
        )

        content.addView(createActionButton(getString(R.string.ar_try_camera_again)) {
            buildCameraUi(prepareArCoreStatus())
            requestCameraOrFallback()
        })

        content.addView(createActionButton(getString(R.string.ar_install_arcore)) {
            openArCoreStorePage()
        })

        content.addView(createActionButton(getString(R.string.close)) {
            finish()
        })

        scrollView.addView(content)
        root.addView(scrollView)
    }

    private fun createActionButton(textValue: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.maroon_button_bg)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                setMargins(0, dp(16), 0, 0)
            }
        }
    }

    private fun openArCoreStorePage() {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=com.google.ar.core")
        )

        try {
            startActivity(marketIntent)
        } catch (exception: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.google.ar.core")
                )
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun roundedBackground(color: String, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = radius.toFloat()
        }
    }
}
