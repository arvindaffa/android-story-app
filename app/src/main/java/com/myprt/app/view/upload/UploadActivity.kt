package com.myprt.app.view.upload

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.myprt.app.databinding.ActivityUploadBinding
import com.myprt.app.util.getImageUri
import com.myprt.app.util.reduceFileImage
import com.myprt.app.util.uriToFile
import kotlinx.coroutines.launch

class UploadActivity : AppCompatActivity() {

    private val viewModel: UploadViewModel by viewModels { UploadViewModel.Factory }
    private lateinit var binding: ActivityUploadBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentImageUri: Uri? = null
    private val isLocationPermissionGranted: MutableLiveData<Boolean> = MutableLiveData()
    private var currentLocation: Location? = null
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    Log.e("UploadFragment", "ACCESS_FINE_LOCATION granted")
                    isLocationPermissionGranted.value = true
                    getLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    Log.e("UploadFragment", "ACCESS_COARSE_LOCATION granted")
                    isLocationPermissionGranted.value = true
                    getLocation()
                }
                else -> {
                    Log.e("UploadFragment", "Location permission denied")
                    isLocationPermissionGranted.value = false
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setActionBar()
        getLocation()
        setListeners()
        observeData()
    }

    private fun getLocation() {
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    isLocationPermissionGranted.value = true
                    currentLocation = location
                } else {
                    binding.locationSwitch.isChecked = false
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setActionBar() {
        supportActionBar?.apply {
            title = "Upload Image"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadUiState.collect {
                    showLoading(false)
                    when (it) {
                        is UploadUiState.Loading -> showLoading(true)
                        is UploadUiState.Success -> {
                            Toast.makeText(
                                this@UploadActivity, it.message, Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        is UploadUiState.Error -> {
                            Toast.makeText(this@UploadActivity, it.errorMessage, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }
        isLocationPermissionGranted.observe(this) { isGranted ->
            binding.locationSwitch.isEnabled = isGranted
        }
    }

    private fun setListeners() {
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.uploadButton.isEnabled = !isLoading
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.descriptionInputLayout.editText?.text.toString().trim()
            val location = if (isLocationPermissionGranted.value == true && binding.locationSwitch.isChecked) currentLocation else null

            viewModel.uploadImage(imageFile, description, location)
        } ?: Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}