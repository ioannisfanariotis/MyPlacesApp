package com.example.placesapp.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.placesapp.databinding.ActivityDetailsBinding
import com.example.placesapp.models.PlaceModel

class DetailsActivity : AppCompatActivity() {

    private var binding: ActivityDetailsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var detailsModel: PlaceModel? = null
        if (intent.hasExtra(MainActivity.EXTRA_DETAILS)){
            detailsModel = intent.getParcelableExtra(MainActivity.EXTRA_DETAILS)
            supportActionBar?.title = detailsModel?.title
        }

        setSupportActionBar(binding?.toolbarDetails)
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title =detailsModel?.title
        }
        binding?.toolbarDetails?.setNavigationOnClickListener{
            onBackPressed()
        }

        if(detailsModel != null){
            binding?.placeImage?.setImageURI(Uri.parse(detailsModel.image))
            binding?.description?.text = detailsModel.description
            binding?.location?.text = detailsModel.location
        }
    }
}