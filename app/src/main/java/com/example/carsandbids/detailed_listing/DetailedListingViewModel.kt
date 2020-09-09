package com.example.carsandbids.detailed_listing

import android.view.View
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.carsandbids.DatabaseSingleton
import com.example.carsandbids.R
import kotlinx.android.synthetic.main.main_listing_tile.view.*

class DetailedListingViewModel(private val position: Int) : ViewModel() {

    private val carClickedMap : Map<String, Any> = DatabaseSingleton.carsInDatabase[position]

    private val carDetailsMap : Map<*, *>
    private val yourInfoMap : Map<*, *>
    private val titleInfoMap : Map<*, *>

    private var year: String
    private var make : String
    private var model : String
    val fullCarName : String

    val descript : String

    val carLoc : String

    val mileage : String

    val mods : String

    val sellerType : String

    val sellerName : String

    val titleStatus : String

    val titleLocation : String

    val firstImage : String

    val reserveTag : Int

    init {

        // get car details map and all its info
        carDetailsMap = carClickedMap["carDetails"] as Map<*, *>

        year = carDetailsMap["year"].toString()
        make = carDetailsMap["make"].toString()
        model = carDetailsMap["model"].toString()
        fullCarName = "$year $make $model"

        descript = carDetailsMap["features"].toString()

        carLoc = carDetailsMap["location"].toString()

        mileage = carDetailsMap["mileage"].toString()

        if (carDetailsMap["modifications"].toString().isEmpty()){
            mods = "No"
        }
        else{
            mods = "Yes"
        }

        // get your info map and get all info
        yourInfoMap = carClickedMap["yourInfo"] as Map<*, *>

        sellerName = yourInfoMap["name"].toString()
        if (yourInfoMap["dealerName"].toString().isEmpty()){
            sellerType = "Private Seller"
        }
        else{
            sellerType = "Dealer"
        }

        // get title info map and get all info
        titleInfoMap = carClickedMap["titleInfo"] as Map<*, *>

        titleStatus = titleInfoMap["titleStatus"].toString()

        titleLocation = titleInfoMap["titleLocation"].toString()

        //get the first image
        //get first image, using Glide
        val imageArray = carClickedMap["imgURls"] as ArrayList<*>
        firstImage = imageArray[0].toString()

        // see if there is a reserve
        if (carClickedMap["reserve"].toString().isEmpty()){
            reserveTag = View.VISIBLE
        }
        else{
            reserveTag = View.GONE
        }

    }

}