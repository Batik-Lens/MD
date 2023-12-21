package com.example.batiklens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.batiklens.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanActivity : AppCompatActivity() {

    private lateinit var camera: Button
    private lateinit var gallery: Button
    private lateinit var result: TextView
    private lateinit var imageView: ImageView
    private val imageSize = 32

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        camera = findViewById(R.id.button)
        gallery = findViewById(R.id.button2)
        result = findViewById(R.id.result)
        imageView = findViewById(R.id.imageView)

        camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@ScanActivity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 3)
            } else {
                ActivityCompat.requestPermissions(
                    this@ScanActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    100
                )
            }
        }

        gallery.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1)
        }
    }

    private fun classifyImage(image: Bitmap) {
        try {
            val model = Model.newInstance(this)

            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
            var pixel = 0

            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val value = intValues[pixel++]
                    byteBuffer.putFloat(((value shr 16) and 0xFF).toFloat() * (1.0f / 1))
                    byteBuffer.putFloat(((value shr 8) and 0xFF).toFloat() * (1.0f / 1))
                    byteBuffer.putFloat((value and 0xFF).toFloat() * (1.0f / 1))
                }
            }

            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.getOutputFeature0AsTensorBuffer()

            val confidences = outputFeature0.floatArray
            var maxPos = 0
            var maxConfidence = 0f

            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }

            val classes = arrayOf(
                "Batik Bali", "Batik Betawi", "Batik Cendrawasih", "Batik Dayak",  "Batik Geblek Renteng",
                "Batik Ikat Celup", "Batik Insang", "Batik Kawung", "Batik Lasem", "Batik Megamendung",
                "Batik Pala", "Batik Parang", "Batik Poleng", "Batik sekar Jagad", "Batik Tambal"
            )
            result.text = classes[maxPos]

            model.close()
        } catch (e: IOException) {
            // Handle the exception
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                3 -> {
                    val image = data?.extras?.get("data") as Bitmap
                    val dimension = Math.min(image.width, image.height)
                    val thumbnail = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
                    imageView.setImageBitmap(thumbnail)
                    classifyImage(Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false))
                }
                1 -> {
                    val selectedImageUri: Uri? = data?.data
                    val image: Bitmap? = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        selectedImageUri
                    )
                    imageView.setImageBitmap(image)
                    classifyImage(Bitmap.createScaledBitmap(image!!, imageSize, imageSize, false))
                }
            }
        }
    }
}
