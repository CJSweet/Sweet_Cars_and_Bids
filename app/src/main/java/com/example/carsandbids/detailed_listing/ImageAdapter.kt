package com.example.carsandbids.detailed_listing

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.carsandbids.R
import com.example.carsandbids.submit_car.PhotoAdapter
import kotlinx.android.synthetic.main.detailed_listing_fragment.view.*
import kotlinx.android.synthetic.main.detailed_recyclerview_imageview.view.*
import java.net.URL

class ImageAdapter(private val photos: ArrayList<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.ViewHolder {
        // create a new view
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.detailed_recyclerview_imageview, parent, false) as ImageView

        // can set more parameters here if desired

        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageAdapter.ViewHolder, position: Int) {

        val requestOptions: RequestOptions = RequestOptions().placeholder(R.drawable.image_loading_icon1)
            .error(R.drawable.error_image_icon).centerCrop()

        Glide.with(holder.itemView).load(photos[position]).apply(requestOptions).into(holder.itemView.detailed_image_view_item)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

}