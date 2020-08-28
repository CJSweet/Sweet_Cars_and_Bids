package com.example.carsandbids.submit_car

data class YourInfo(
    val name: String ?= null,
    val number: String ?= null,
    val addFees: String ?= null,
    val dealerName: String ?= null,
    val dealerWeb: String ?= null
)

data class CarDetails(
    val saleElsewhere: Boolean ?= false,
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

data class ReservePrice(
    val reserve: String
)

data class Referral(
    val referral: String ?= null
)

data class AllInfo(
    val yourInfo: YourInfo,
    val carDetails: CarDetails,
    val titleInfo: TitleInfo,
    val reservePrice: ReservePrice,
    val imgURls: ArrayList<String>,
    val referral: Referral
)