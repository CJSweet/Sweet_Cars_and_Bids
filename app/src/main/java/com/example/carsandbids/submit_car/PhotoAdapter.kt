package com.example.carsandbids.submit_car

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carsandbids.R
import kotlinx.android.synthetic.main.photo_item.view.*
import kotlinx.android.synthetic.main.submit_car.view.*

class PhotoAdapter(private val photos: ArrayList<Bitmap>, val onDeletePhotoListener: OnDeletePhotoListener) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a ImageView.
    class PhotoViewHolder(val imageView: LinearLayout, val onDeletePhotoListener: OnDeletePhotoListener) : RecyclerView.ViewHolder(imageView), View.OnClickListener{

        init {
            imageView.submit_delete_text.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onDeletePhotoListener.onDeletePhotoClick(adapterPosition)
        }
    }

    //create a new view (invoked by layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAdapter.PhotoViewHolder {

        //create a new view
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.photo_item, parent, false) as LinearLayout

        //can set more parameters here if desired

        return PhotoAdapter.PhotoViewHolder(imageView, onDeletePhotoListener)
    }

    //return the size of the data set,
    override fun getItemCount(): Int {
        if (photos.size > 0)
            return photos.size
        else return 0
    }

    //Replace the contents of a view (also invoked by the layout manager)
    override fun onBindViewHolder(holder: PhotoAdapter.PhotoViewHolder, position: Int) {
        // get element from data set (in this case, the URI)
        // replace the contents of the image view with the new URI
        holder.imageView.submit_photo_view.setImageBitmap(photos[position])

        holder.imageView.submit_delete_text.setOnClickListener {
            photos.removeAt(position)
        }
    }

    interface OnDeletePhotoListener{
        fun onDeletePhotoClick(position: Int)
    }
}