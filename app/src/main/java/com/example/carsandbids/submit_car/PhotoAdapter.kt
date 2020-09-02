package com.example.carsandbids.submit_car

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.carsandbids.R
import kotlinx.android.synthetic.main.submit_photo_item.view.*

class PhotoAdapter(private val photos: ArrayList<Bitmap>, val onDeletePhotoListener: OnDeletePhotoListener) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a ImageView.
    inner class PhotoViewHolder(val imageView: LinearLayout) : RecyclerView.ViewHolder(imageView), View.OnClickListener{

        init {
            imageView.submit_delete_text.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            // https://www.youtube.com/watch?v=wKFJsrdiGS8
            // this way, there is no chance of clicking a view holder during
            // the time it is being deleted
            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                onDeletePhotoListener.onDeletePhotoClick(adapterPosition)
            }
        }
    }

    // create a new view (invoked by layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAdapter.PhotoViewHolder {

        // create a new view
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.submit_photo_item, parent, false) as LinearLayout

        // can set more parameters here if desired

        return PhotoViewHolder(imageView)
    }

    // return the size of the data set,
    override fun getItemCount(): Int {
        if (photos.size > 0)
            return photos.size
        else return 0
    }

    // Replace the contents of a view (also invoked by the layout manager)
    override fun onBindViewHolder(holder: PhotoAdapter.PhotoViewHolder, position: Int) {
        // get element from data set (in this case, the Bitmap)
        // replace the contents of the image view with the new bitmap
        holder.imageView.submit_photo_view.setImageBitmap(photos[position])
    }

    interface OnDeletePhotoListener{
        fun onDeletePhotoClick(position: Int)
    }
}