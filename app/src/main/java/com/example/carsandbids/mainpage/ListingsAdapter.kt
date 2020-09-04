package com.example.carsandbids.mainpage

import android.content.Context
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.carsandbids.R
import kotlinx.android.synthetic.main.main_listing_tile.view.*

class ListingsAdapter(val context: Context, private val listings: ArrayList<Map<String, Any>>) : RecyclerView.Adapter<ListingsAdapter.ListingViewHolder>() {

    inner class ListingViewHolder (val listing: CardView) : RecyclerView.ViewHolder(listing){

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
        val reserveMap = listings[position].get("reservePrice") as Map<*, *>
        val reserve = reserveMap["reserve"].toString()
        if (reserve.isEmpty()){
            holder.listing.tile_current_price.visibility = View.GONE
        }
        else{
            val bidAndReserve = "<font color=#C1C1C1>Bid:</font> <font color=#ffffff>$${reserve} </font>"
            holder.listing.tile_current_price.setText(Html.fromHtml(bidAndReserve, Html.FROM_HTML_MODE_LEGACY))
        }


        //This is where the notable features will go for now
        val features = carDetailsMap["features"].toString()
        holder.listing.tile_car_descript.setText(features)

        val year = carDetailsMap["year"].toString()
        val make = carDetailsMap["make"].toString()
        val model = carDetailsMap["model"].toString()

        val fullCarName = year + " " + make + " "+ model
        holder.listing.tile_car_name.setText(fullCarName)
        holder.listing.tile_time_left.setText("5:38:42")

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

}