package com.example.carsandbids.mainpage

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.carsandbids.R
import com.example.carsandbids.generated.callback.OnClickListener
import kotlinx.android.synthetic.main.main_listing_tile.view.*
import java.util.concurrent.TimeUnit

class ListingsAdapter(val context: Context, private val listings: ArrayList<Map<String, Any>>, val onClickCard: OnClickCardListener) : RecyclerView.Adapter<ListingsAdapter.ListingViewHolder>() {

    private val SEVEN_DAYS: Long = 604800000

    inner class ListingViewHolder (val listing: CardView) : RecyclerView.ViewHolder(listing), View.OnClickListener{

        init {
            listing.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            // https://www.youtube.com/watch?v=wKFJsrdiGS8
            // this way, there is no chance of clicking a view holder during
            // the time it is being deleted
            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                onClickCard.onClickCard(listings[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListingsAdapter.ListingViewHolder {
        //Inflate the XML
        val listing = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_listing_tile, parent, false) as CardView


        return ListingViewHolder(listing)
    }

    override fun onBindViewHolder(holder: ListingsAdapter.ListingViewHolder, position: Int) {
        //Here is where we can bind all the items we like
        val carDetailsMap = listings[position].get("carDetails") as Map<*, *>

        //this is for current price, but for now, it will be reserve
        val reserve = listings[position].get("reserve") as String?
        if (reserve!!.isEmpty()){
            holder.listing.tile_current_price.visibility = View.GONE
            holder.listing.listing_no_reserve_view.visibility = View.VISIBLE
        }
        else{
            val bidAndReserve = "<font color=#C1C1C1>Bid:</font> <font color=#ffffff>$${reserve} </font>"
            holder.listing.tile_current_price.text = Html.fromHtml(bidAndReserve, Html.FROM_HTML_MODE_LEGACY)
        }


        //This is where the notable features will go for now
        val features = carDetailsMap["features"].toString()
        holder.listing.tile_car_descript.setText(features)

        val year = carDetailsMap["year"].toString()
        val make = carDetailsMap["make"].toString()
        val model = carDetailsMap["model"].toString()

        val fullCarName = year + " " + make + " "+ model
        holder.listing.tile_car_name.setText(fullCarName)

        //Time till auction ends

        // Get the timestamp of when the listing was created
        val timestamp = listings[position].get("timestamp") as Long
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
                    holder.listing.tile_time_left.text = timeTill
                }

                override fun onFinish() {
                    holder.listing.tile_time_left.text = "SOLD"
                }
            }
            timer.start()
        }
        else{
            holder.listing.tile_time_left.text = "SOLD"
        }

        //Get the car location
        val carLoc = carDetailsMap["location"].toString()
        holder.listing.listing_car_location.setText(carLoc)

        //get first image, using Glide
        val imageArray = listings[position].get("imgURls") as ArrayList<*>
        val requestOptions: RequestOptions = RequestOptions().placeholder(R.drawable.image_loading_icon1)
            .error(R.drawable.error_image_icon).centerCrop()

        Glide.with(context).load(imageArray[0]).apply(requestOptions).into(holder.listing.tile_image)
    }

    override fun getItemCount(): Int {
        return listings.size
    }

    interface OnClickCardListener{
        fun onClickCard(listing: Map<String, Any>)
    }

}