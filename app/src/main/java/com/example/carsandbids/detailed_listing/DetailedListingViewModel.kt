package com.example.carsandbids.detailed_listing

import android.os.CountDownTimer
import android.text.Html
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    val stock : String

    val sellerType : String

    val sellerName : String

    val titleStatus : String

    val titleLocation : String

    val reserveTag : Int
    val reservePrice : String

    val imageArray : ArrayList<String>

    private var _timeToClose = MutableLiveData<String>()
    val timeToClose : LiveData<String>
        get() = _timeToClose
    private val SEVEN_DAYS: Long = 604800000

    init {

        // get car details map and all its info
        carDetailsMap = carClickedMap["carDetails"] as Map<*, *>

        year = carDetailsMap["year"].toString()
        make = carDetailsMap["make"].toString()
        model = carDetailsMap["model"].toString()
        fullCarName = "$year $make $model"

        descript = carDetailsMap["features"].toString()

        carLoc = carDetailsMap["location"].toString()

        mileage = "<u>Mileage</u>: ${carDetailsMap["mileage"].toString()}"

        if (carDetailsMap["modifications"].toString().isEmpty()){
            stock = "<u>Completely Stock</u>: Yes"
        }
        else{
            stock = "<u>Completely Stock</u>: No"
        }

        // get your info map and get all info
        yourInfoMap = carClickedMap["yourInfo"] as Map<*, *>

        sellerName = "<u>Seller</u>: ${yourInfoMap["name"].toString()}"
        if (yourInfoMap["dealerName"].toString().isEmpty()){
            sellerType = "<u>Seller Type</u>: Private Seller"
        }
        else{
            sellerType = "<u>Seller Type</u>: Dealer"
        }

        // get title info map and get all info
        titleInfoMap = carClickedMap["titleInfo"] as Map<*, *>

        titleStatus = "<u>Title Status</u>: ${titleInfoMap["titleStatus"].toString()}"

        titleLocation = "<u>Title Location</u> ${titleInfoMap["titleLocation"].toString()}"

        //get image array, using Glide
        imageArray = carClickedMap["imgURls"] as ArrayList<String>

        // see if there is a reserve
        if (carClickedMap["reserve"].toString().isEmpty()){
            reserveTag = View.VISIBLE
        }
        else{
            reserveTag = View.GONE
        }

        //Get Time left
        //Calculate Time left
        // Get the timestamp of when the listing was created
        val timestamp = carClickedMap.get("timestamp") as Long
        //add timestamp and 7 days to find the time till auction ends
        val ending = timestamp.plus(SEVEN_DAYS)
        //get current time
        val now = System.currentTimeMillis()
        //get difference
        val timeTillEnd = ending - now
        //countdown from now till end
        if (timeTillEnd > 0){
            val timer = object: CountDownTimer(timeTillEnd, 1000){
                override fun onTick(millisUntilFinished: Long) {
                    val secondsTill = millisUntilFinished / 1000 % 60
                    val minutesTill = millisUntilFinished / 1000 / 60 % 60
                    val hoursTill = millisUntilFinished / 1000 / 60 / 60 % 24
                    val daysTill = millisUntilFinished / 1000 / 60 / 60 / 24
                    val timeTill : String = "$daysTill:$hoursTill:$minutesTill:$secondsTill"
                    // TODO: Make it so if second, minute, or hour are down to single digit,
                    //  have a leading zero
                    _timeToClose.value = timeTill
                }

                override fun onFinish() {
                    _timeToClose.value = "SOLD"
                }
            }
            timer.start()
        }
        else{
            _timeToClose.value = "SOLD"
        }

        //reserve price
        val dbReserve = carClickedMap.get("reserve") as String
        if (dbReserve.isEmpty()){
            reservePrice = "000000"
        }
        else{
            reservePrice = "<font color=#C1C1C1>High Bid</font> <font color=#ffffff>$${dbReserve} </font>"
        }

    }

}