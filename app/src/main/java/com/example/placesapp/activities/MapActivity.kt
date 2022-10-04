package com.example.placesapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.placesapp.R
import com.example.placesapp.databinding.ActivityMapBinding
import com.example.placesapp.models.PlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var binding: ActivityMapBinding? = null
    private var spot: PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.mapToolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.mapToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_DETAILS)) {
            spot = intent.getParcelableExtra(MainActivity.EXTRA_DETAILS)
        }

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(spot!!.latitude, spot!!.longitude)
        googleMap.addMarker(MarkerOptions().position(position).title(spot!!.location))
        val zoom = CameraUpdateFactory.newLatLngZoom(position, 10f)
        googleMap.animateCamera(zoom)
    }
}