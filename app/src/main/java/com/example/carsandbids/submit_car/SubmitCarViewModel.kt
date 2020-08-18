package com.example.carsandbids.submit_car

import android.graphics.Color
import android.widget.Toast
import androidx.lifecycle.ViewModel

class SubmitCarViewModel : ViewModel() {

    var colorB = Color.BLACK

    fun onButtonClick(){
        //when button is clicked, flip the colors of background and
        // text
        println("button pressed")
    }
}