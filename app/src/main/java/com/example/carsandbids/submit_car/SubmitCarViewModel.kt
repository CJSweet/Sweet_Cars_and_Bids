package com.example.carsandbids.submit_car

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.collections.ArrayList

class SubmitCarViewModel : ViewModel() {

    private val _dealerInfoView = MutableLiveData<Int>()
    val dealerInfoView: LiveData<Int>
        get() = _dealerInfoView

    private val _carListingsView = MutableLiveData<Int>()
    val carListingsView: LiveData<Int>
        get() = _carListingsView

    private val _carLocationUSView = MutableLiveData<Int>()
    val carLocationUSView: LiveData<Int>
        get() = _carLocationUSView

    private val _carLocationCanView = MutableLiveData<Int>()
    val carLocationCanView: LiveData<Int>
        get() = _carLocationCanView

    private val _carModifiedView = MutableLiveData<Int>()
    val carModifiedView: LiveData<Int>
        get() = _carModifiedView

    private val _titleLocUSView = MutableLiveData<Int>()
    val titleLocUSView: LiveData<Int>
        get() = _titleLocUSView

    private val _titleLocCanView = MutableLiveData<Int>()
    val titleLocCanView: LiveData<Int>
        get() = _titleLocCanView

    private val _titleNameView = MutableLiveData<Int>()
    val titleNameView: LiveData<Int>
        get() = _titleNameView

    private val _reserveView = MutableLiveData<Int>()
    val reserveView: LiveData<Int>
        get() = _reserveView


    private val _extraLinksNum = MutableLiveData<Int>()
    val extraLinksNum: LiveData<Int>
        get() = _extraLinksNum

    //determine if helper text can be displayed on photo
    var photoTextVis = MutableLiveData<Int>()

    //determines if clear button can be displayed for photos
    var clrBtnVis = MutableLiveData<Int>()

    //arraylist for the photos added to submit page
    var imgBitmaps = ArrayList<Bitmap>()


    //initialize all variable views to GONE
    init {
        _dealerInfoView.value = View.GONE
        _carListingsView.value = View.GONE
        _carLocationUSView.value = View.GONE
        _carLocationCanView.value = View.GONE
        _carModifiedView.value = View.GONE
        _titleLocUSView.value = View.GONE
        _titleLocCanView.value = View.GONE
        _titleNameView.value = View.GONE
        _reserveView.value = View.GONE

        _extraLinksNum.value = 0

        photoTextVis.value = View.VISIBLE
        clrBtnVis.value = View.GONE
    }

    //if user selects dealer as seller, then display dealer info views
    fun dealerInfoVisible(showDealerInfo: Boolean) {

        if (!showDealerInfo) {
            _dealerInfoView.value = View.GONE
        } else {
            _dealerInfoView.value = View.VISIBLE
        }
    }

    //if user selects car is for sale elsewhere, show elsewhere warning and views
    fun carListingsVisible(showCarListings: Boolean) {

        if (!showCarListings)
            _carListingsView.value = View.GONE
        else
            _carListingsView.value = View.VISIBLE
    }

    //when user selects where car is located (US or CAN), show proper views
    // zip code for US and City/Province for CAN
    // parameter true means car is in US
    // parameter false means car is in CAN
    fun carLocationVisible(showCarLocation: Boolean) {
        if (showCarLocation) {
            _carLocationUSView.value = View.VISIBLE
            _carLocationCanView.value = View.GONE
        } else {
            //Invisible, instead of View.GONE so layout margins are maintained
            // for the views below
            _carLocationUSView.value = View.INVISIBLE
            _carLocationCanView.value = View.VISIBLE
        }
    }

    //if car is stock, true
    //if car has been modified, false
    fun isCarStock(carStock: Boolean) {
        if (carStock)
            _carModifiedView.value = View.GONE
        else
            _carModifiedView.value = View.VISIBLE
    }

    //if title in US, true
    //if title in CAN, false
    fun isTitleInUS(titleLoc: Boolean) {
        if (titleLoc) {
            _titleLocUSView.value = View.VISIBLE
            _titleLocCanView.value = View.GONE
        } else {
            _titleLocUSView.value = View.INVISIBLE
            _titleLocCanView.value = View.VISIBLE
        }
    }

    //if title in user name, true
    //if title in other name, false
    fun isTitleInUserName(titleName: Boolean) {
        if (titleName) {
            _titleNameView.value = View.GONE
        } else
            _titleNameView.value = View.VISIBLE
    }

    fun wantAReserve(wantsReserve: Boolean) {
        if (wantsReserve) {
            _reserveView.value = View.VISIBLE
        } else {
            _reserveView.value = View.GONE
        }
    }

    fun onClrBtnClick() {
        imgBitmaps.clear()
        photoTextVis.value = View.VISIBLE
    }

}