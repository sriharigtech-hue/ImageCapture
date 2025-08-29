package com.example.imagelabellingapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var objectImage: ImageView
    private lateinit var labelText: TextView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var captureBtn: Button
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageLabeler: ImageLabeler

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        objectImage = findViewById(R.id.objectImage)
        labelText = findViewById(R.id.labelText)
        captureBtn = findViewById(R.id.captureBtn)
        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        checkCameraPermission()

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val extras = result.data?.extras
                val imageBitmap = extras?.getParcelable("data", Bitmap::class.java)
                if (imageBitmap != null) {
                    objectImage.setImageBitmap(imageBitmap)
                    labelImage(imageBitmap)
                } else {
                    labelText.text = "No Image"
                }
            }
        }
        captureBtn.setOnClickListener {
            val clickPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (clickPicture.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(clickPicture)
            } else {
                labelText.text = "No Camera"

            }
        }
    }
    private fun labelImage(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        imageLabeler.process(inputImage).addOnSuccessListener { labels ->
            displayLabel(labels)

        }.addOnFailureListener { e ->
            labelText.text = "Error: ${e.message}"
        }



    }


    private fun displayLabel(labels: List<ImageLabel>) {
        if (labels.isNotEmpty()){
            val mostConfidentLabel = labels[0]
            labelText.text = "${mostConfidentLabel.text}"
        } else {
            labelText.text = "No labels found"
        }

    }


    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                1
            )
        }
    }
}