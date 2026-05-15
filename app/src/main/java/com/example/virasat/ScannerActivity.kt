package com.example.virasat

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.virasat.data.HeritageRepository
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage

class ScannerActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PICK_QR_IMAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScannerChoiceScreen()
    }

    private fun showScannerChoiceScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(Color.parseColor("#F3E2C7"))
        }

        val title = TextView(this).apply {
            text = "Unlock Hidden Fact"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#6E1F14"))
        }

        val subtitle = TextView(this).apply {
            text = "Scan a Virasat QR using your camera, or choose a screenshot from your gallery."
            textSize = 15f
            setTextColor(Color.parseColor("#231815"))
            setPadding(0, 10, 0, 28)
            setLineSpacing(5f, 1.0f)
        }

        val cameraButton = Button(this).apply {
            text = getString(R.string.scan_with_camera)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.maroon_button_bg)
            setOnClickListener {
                scanUsingCamera()
            }
        }

        val galleryButton = Button(this).apply {
            text = getString(R.string.pick_from_gallery)
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.brown_button_bg)
            setOnClickListener {
                pickImageFromGallery()
            }
        }

        val buttonParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(58)
        ).apply {
            setMargins(0, 0, 0, 16)
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(cameraButton, buttonParams)
        layout.addView(galleryButton, buttonParams)

        setContentView(layout)
    }

    private fun scanUsingCamera() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                handleQrValue(barcode.rawValue)
            }
            .addOnCanceledListener {
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.scanner_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }

        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.choose_qr_image)),
            REQUEST_PICK_QR_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PICK_QR_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri = data?.data ?: return
            scanQrFromImage(imageUri)
        }
    }

    private fun scanQrFromImage(imageUri: Uri) {
        val image = InputImage.fromFilePath(this, imageUri)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val qrValue = barcodes.firstOrNull()?.rawValue

                if (qrValue == null) {
                    Toast.makeText(
                        this,
                        getString(R.string.no_qr_found),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    handleQrValue(qrValue)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.scanner_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnCompleteListener {
                scanner.close()
            }
    }

    private fun handleQrValue(qrValue: String?) {
        if (qrValue == null) {
            Toast.makeText(
                this,
                getString(R.string.unknown_qr_code),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (qrValue.startsWith("virasat://hiddenfact")) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(qrValue)
            startActivity(intent)
            finish()
            return
        }

        if (qrValue.startsWith("VIRASAT_FACT|")) {
            val parts = qrValue.split("|", limit = 3)

            if (parts.size == 3) {
                val siteId = parts[1]
                val hiddenFact = parts[2]

                val intent = Intent(this, HiddenFactActivity::class.java)
                intent.putExtra(HiddenFactActivity.EXTRA_SITE_ID, siteId)
                intent.putExtra(HiddenFactActivity.EXTRA_HIDDEN_FACT, hiddenFact)
                startActivity(intent)
                finish()
                return
            }
        }

        val site = HeritageRepository.getSiteById(qrValue)

        if (site != null) {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_SITE_ID, site.id)
            intent.putExtra(MainActivity.EXTRA_HIDDEN_FACT_UNLOCKED, true)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(
                this,
                getString(R.string.unknown_qr_code),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
