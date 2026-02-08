package com.dicoding.picodiploma.loginwithanimation.view.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.UploadRepository
import com.dicoding.picodiploma.loginwithanimation.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.response.StoryResponse
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.story.StoryDetailActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java
import androidx.core.graphics.scale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var apiService: ApiService
    private lateinit var mainViewModel: MainViewModel
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        apiService = ApiConfig.getMapApiService()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val viewModelFactory = ViewModelFactory.getInstance(applicationContext)
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        mainViewModel.getSession().observe(this, Observer { user ->
            if (user != null && user.isLogin) {
                token = user.token
                fetchStoriesWithLocation(token)
            } else {
                Toast.makeText(this, "Anda belum login!", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setCustomMapStyle()

        val dicodingSpace = LatLng(-6.728840625335708, 108.57403928650656)
        mMap.addMarker(
            MarkerOptions()
                .position(dicodingSpace)
                .title("Rumah Dandy")
                .snippet("Roemah Mbah Caca")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dicodingSpace, 15f))
    }

    private fun setCustomMapStyle() {
        try {
            val context = applicationContext
            val inputStream = context.resources.openRawResource(R.raw.style)
            val style = String(inputStream.readBytes())

            val success = mMap.setMapStyle(MapStyleOptions(style))
            if (!success) {
                Toast.makeText(this, "Gagal memuat custom map style", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error memuat custom style: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStoriesWithLocation(token: String) {
        apiService.getStoriesLocation("Bearer $token").enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    val stories = response.body()?.listStory ?: return
                    displayMarkersOnMap(stories)
                } else {
                    Toast.makeText(this@MapsActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayMarkersOnMap(stories: List<ListStoryItem>) {
        mMap.clear()

        for (story in stories) {
            if (story.lat != null && story.lon != null) {
                val location = LatLng(story.lat, story.lon)

                val markerOptions = MarkerOptions()
                    .position(location)
                    .title(story.name)
                    .snippet(story.description)

                if (story.photoUrl != null) {
                    Glide.with(this)
                        .asBitmap()
                        .load(story.photoUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                val scaleBitmap =
                                    resource.scale(100, 100, false)
                                val marker = mMap.addMarker(
                                    markerOptions.icon(
                                        BitmapDescriptorFactory.fromBitmap(scaleBitmap)
                                    )
                                )
                                marker?.tag = story.id
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                        } else {
                            val marker = mMap.addMarker(markerOptions)
                            marker?.tag = story.id
                        }
                    }
                }

            mMap.setOnMarkerClickListener { marker ->
                val storyId = marker.tag as? String
                storyId?.let {
                    val intent = Intent(this, StoryDetailActivity::class.java)
                    intent.putExtra(StoryDetailActivity.EXTRA_ID, it)
                    startActivity(intent)
                }
                true
                }
    }
}