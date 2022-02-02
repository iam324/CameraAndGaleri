package com.example.cameraandgaleri

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.cameraandgaleri.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var photoFile: File
    private val photoName = "photo.jpg"
    private val TAG = "cameraapp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoFile = getPhotoFileUri(photoName)
    }

    private var activitylauncherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.imageView.setImageBitmap(takenImage)
        }
    private var activitylauncherGallery= registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
        result.data?.data?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28){
                    MediaStore.Images.Media.getBitmap(this.contentResolver, it)

                }else{
                    val source = ImageDecoder.createSource(this.contentResolver,it)
                    ImageDecoder.decodeBitmap(source)
                }
                binding.imageView.setImageBitmap(bitmap)

            }catch (e: Exception){
                e.printStackTrace()
            }
        }

    }

    private fun openCamera() {
        val fileProvider = FileProvider.getUriForFile(this, "com.example.fileProvider", photoFile)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        }

        activitylauncherCamera.launch(takePictureIntent)
    }
    private fun oppenGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activitylauncherGallery.launch(galleryIntent)

    }

    private fun getPhotoFileUri(filename: String): File {
        val mediaStorageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdir()) {
            Log.d(TAG, "failed to create directory")

        }
        return File(mediaStorageDir.path + File.separator + filename)


    }

    private fun checkPermissionCamera(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun checkPermissionGallery(): Boolean{
        return ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionCamera() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
    }
    private fun requestPermissionGallery() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 110)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openCamera()
            } else {
                Toast.makeText(this, "user not give the permissions for camera", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (requestCode == 110) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                oppenGallery()
            } else {
                Toast.makeText(
                    this,
                    "user not give the permissions for gallery",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

        override fun onClick(view: View?) {
            when (view) {
                binding.btnCamera -> {
                    if (checkPermissionCamera()) {
                        openCamera()

                    } else {
                        requestPermissionCamera()

                    }

                }
                binding.btnGallery -> {
                    if (checkPermissionGallery()) {
                        oppenGallery()

                    } else {
                        requestPermissionGallery()
                    }
                }
            }
        }

}