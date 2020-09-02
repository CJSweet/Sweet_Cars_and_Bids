package com.example.carsandbids.mainpage

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.carsandbids.R
import kotlinx.android.synthetic.main.main_listing_tile.view.*

class ListingsAdapter(private val listings: ArrayList<Map<String, Any>>) : RecyclerView.Adapter<ListingsAdapter.ListingViewHolder>() {

    inner class ListingViewHolder (val listing: LinearLayout) : RecyclerView.ViewHolder(listing){

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListingsAdapter.ListingViewHolder {
        //Inflate the XML
        val listing = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_listing_tile, parent, false) as LinearLayout


        return ListingViewHolder(listing)
    }

    override fun onBindViewHolder(holder: ListingsAdapter.ListingViewHolder, position: Int) {
        //Here is where we can bind all the items we like
        val carDetailsMap = listings[position].get("carDetails") as Map<*, *>

        //this is for current price, but for now, it will be reserve
        val reserveMap = listings[position].get("reservePrice") as Map<*, *>
        val reserve = reserveMap["reserve"].toString()
        holder.listing.tile_current_price.setText(reserve)

        //This is where the notable features will go for now
        val features = carDetailsMap["features"].toString()
        holder.listing.tile_car_descript.setText(features)

        val make = carDetailsMap["make"].toString()

        val modelMap = listings[position].get("carDetails") as Map<*,*>
        val model = modelMap["model"].toString()

        val makeAndModel = make + " "+ model
        holder.listing.tile_car_name.setText(makeAndModel)
        holder.listing.tile_time_left.setText("So much time left")
    }

    override fun getItemCount(): Int {
        return listings.size
    }

}