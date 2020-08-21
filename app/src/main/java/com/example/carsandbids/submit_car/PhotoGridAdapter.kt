package com.example.carsandbids.submit_car

import android.content.Context
import android.media.Image
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.carsandbids.R

class PhotoGridAdapter(var context: Context, var arrayList: ArrayList<Uri>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = View.inflate(context, R.layout.photo_item, null) as ImageView

        view.setImageURI(arrayList[position])

        return view
    }

    override fun getItem(position: Int): Any {
        return arrayList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return arrayList.size
    }
}