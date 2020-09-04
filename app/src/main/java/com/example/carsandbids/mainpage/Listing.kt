package com.example.carsandbids.mainpage

import java.io.Serializable

// This is for when a user selects a car on the main page to view this class will be passed
// into the detailed page

class Listing(val listing: Map<String, Any>) : Serializable {
    private val fullCarName : String
     get() {
         return "${carDetailsMap["year"].toString()} ${carDetailsMap["make"].toString()} ${carDetailsMap["model"].toString()}"
     }

    private val carDescript : String
        get() {
            return carDetailsMap["features"].toString()
        }

    private val carDetailsMap : Map<String, *>
        get() {
            return listing["carDetails"] as Map<String, *>
        }

    init {

    }



}