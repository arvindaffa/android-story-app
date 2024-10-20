package com.myprt.app.view.maps

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.myprt.app.R
import com.myprt.app.databinding.ActivityMapsBinding
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel: MapsViewModel by viewModels { MapsViewModel.Factory }
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setActionBar()
        setMapStyle()
        addManyMarker()
    }

    private fun setActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Maps"
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

    private fun addManyMarker() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mapsUiState.collect {
                    binding.progressBar.isVisible = it is MapsUiState.Loading

                    when (it) {
                        is MapsUiState.Success -> {
                            it.listStory.forEach { story ->
                                if (story.lat != null && story.lon != null) {
                                    val position = LatLng(story.lat, story.lon)
                                    val marker = MarkerOptions().position(position)
                                        .title(story.name)
                                        .snippet(story.description)
                                    mMap.addMarker(marker)
                                    boundsBuilder.include(position)
                                }
                            }

                            try {
                                val bounds: LatLngBounds = boundsBuilder.build()
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds,
                                        resources.displayMetrics.widthPixels,
                                        resources.displayMetrics.heightPixels,
                                        300
                                    )
                                )
                            } catch (e: Exception) {
                                Toast.makeText(this@MapsActivity, "No Story With Location", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, e.localizedMessage.orEmpty(), e)
                            }
                        }
                        is MapsUiState.Error -> {
                            Toast.makeText(this@MapsActivity, it.errorMessage, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, it.errorMessage)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}