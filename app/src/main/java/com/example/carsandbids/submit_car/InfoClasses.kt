package com.example.carsandbids.submit_car

import android.net.Uri
import com.google.firebase.storage.UploadTask

data class YourInfo(
    val name: String ?= null,
    val number: String ?= null,
    val addFees: String ?= null,
    val dealerName: String ?= null,
    val dealerWeb: String ?= null
)

data class CarDetails(
    val saleElsewhere: ArrayList<String>,
    val year: String,
    val make: String,
    val model: String,
    val vin: String,
    val mileage: String,
    val location: String,
    val features: String,
    val modifications: String
)

data class TitleInfo(
    val titleLocation: String,
    val nameTitle: String,
    val lienholder: Boolean,
    val titleStatus: String
)

data class AllInfo(
    val timestamp: Long,
    val yourInfo: YourInfo,
    val carDetails: CarDetails,
    val titleInfo: TitleInfo,
    val reserve: String?,
    val imgURls: ArrayList<String>,
    val referral: String?
)