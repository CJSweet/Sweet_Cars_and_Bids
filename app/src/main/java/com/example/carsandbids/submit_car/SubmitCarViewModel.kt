package com.example.carsandbids.submit_car

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.collections.ArrayList

/**
 * ViewModel for SubmitCarFragment
 */
class SubmitCarViewModel : ViewModel() {

    // Keep track of dealer info visibility
    private val _dealerInfoView = MutableLiveData<Int>()
    val dealerInfoView: LiveData<Int>
        get() = _dealerInfoView

    // Keep track of other Listings visibility
    private val _carListingsView = MutableLiveData<Int>()
    val carListingsView: LiveData<Int>
        get() = _carListingsView

    // Keep track of visibility car location US
    private val _carLocationUSView = MutableLiveData<Int>()
    val carLocationUSView: LiveData<Int>
        get() = _carLocationUSView

    // Keep track of visibility car location CAN
    private val _carLocationCanView = MutableLiveData<Int>()
    val carLocationCanView: LiveData<Int>
        get() = _carLocationCanView

    // Keep track of visibility car modified EditText
    private val _carModifiedView = MutableLiveData<Int>()
    val carModifiedView: LiveData<Int>
        get() = _carModifiedView

    // Keep track of visibility title loc US spinner
    private val _titleLocUSView = MutableLiveData<Int>()
    val titleLocUSView: LiveData<Int>
        get() = _titleLocUSView

    // Keep track of visibility title loc CAN spinner
    private val _titleLocCanView = MutableLiveData<Int>()
    val titleLocCanView: LiveData<Int>
        get() = _titleLocCanView

    // Keep track of visibility other name of title EditText
    private val _titleNameView = MutableLiveData<Int>()
    val titleNameView: LiveData<Int>
        get() = _titleNameView

    // Keep track of visibility entering reserve EditText
    private val _reserveView = MutableLiveData<Int>()
    val reserveView: LiveData<Int>
        get() = _reserveView

    // any following variables ending with Error are for
    // managing the viewing of the error textviews
    private val _dealerError = MutableLiveData<Int>()
    val dealerError: LiveData<Int>
        get() = _dealerError

    // to view.VISIBLE sale elsewhere radio error
    private val _saleError = MutableLiveData<Int>()
    val saleError: LiveData<Int>
        get() = _saleError

    // to view.VISIBLE year spinner error
    private val _yearError = MutableLiveData<Int>()
    val yearError: LiveData<Int>
        get() = _yearError

    // to view.VISIBLE or not view.VISIBLE the car location error radio
    private val _carLocError = MutableLiveData<Int>()
    val carLocError: LiveData<Int>
        get() = _carLocError

    // to view.VISIBLE modified radiogroup error
    private val _modGroupError = MutableLiveData<Int>()
    val modGroupError: LiveData<Int>
        get() = _modGroupError

    // to view.VISIBLE title location radiogroup error
    private val _titleLocError = MutableLiveData<Int>()
    val titleLocError: LiveData<Int>
        get() = _titleLocError

    // to view.VISIBLE the title location spinner error
    private val _titleLocSpinError = MutableLiveData<Int>()
    val titleLocSpinError: LiveData<Int>
        get() = _titleLocSpinError

    // to view.VISIBLE the title name error
    private val _titleNameError = MutableLiveData<Int>()
    val titleNameError: LiveData<Int>
        get() = _titleNameError

    // to view.VISIBLE the lienholder error
    private val _lienError = MutableLiveData<Int>()
    val lienError: LiveData<Int>
        get() = _lienError

    // to view.VISIBLE title status error
    private val _titleStatusError = MutableLiveData<Int>()
    val titleStatusError: LiveData<Int>
        get() = _titleStatusError

    // to view.VISIBLE photo error message
    private val _photoError = MutableLiveData<Int>()
    val photoError: LiveData<Int>
        get() = _photoError

    // to view.VISIBLE title status error
    private val _reserveError = MutableLiveData<Int>()
    val reserveError: LiveData<Int>
        get() = _reserveError

    // determine if helper text can be displayed on photo
    var photoTextVis = MutableLiveData<Int>()

    // determines if clear button can be displayed for photos
    var clrBtnVis = MutableLiveData<Int>()

    // arraylist for the photos added to submit page
    var imgBitmaps = ArrayList<Bitmap>()

    // Declaring a storage reference to upload images to storage
    private lateinit var storageRef: StorageReference

    // Declaring a firestore reference to upload all data
    private lateinit var firestore: FirebaseFirestore

    // List of EditTexts for the links users may need for where car is being sold
    var linksList = ArrayList<EditText>()

    //initialize all variables defined above
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

        photoTextVis.value = View.VISIBLE
        clrBtnVis.value = View.GONE

        _dealerError.value = View.GONE
        _saleError.value = View.GONE
        _yearError.value = View.GONE
        _carLocError.value = View.GONE
        _modGroupError.value = View.GONE
        _titleLocError.value = View.GONE
        _titleLocSpinError.value = View.GONE
        _titleNameError.value = View.GONE
        _lienError.value = View.GONE
        _titleStatusError.value = View.GONE
        _reserveError.value = View.GONE
        _photoError.value = View.GONE

        //initialize storage reference
        storageRef = FirebaseStorage.getInstance().getReference("images")

        //initialize firestore reference
        firestore = Firebase.firestore
    }

    /**
     * if user selects dealer as seller, then display dealer info views
     *
     * @param seeDealerInfo - if false view.GONE dealer info, else view.VISIBLE it
     */
    fun dealerInfoVisible(seeDealerInfo: Boolean) {

        if (!seeDealerInfo) {
            _dealerInfoView.value = View.GONE

        } else {
            _dealerInfoView.value = View.VISIBLE
        }
    }

    /**
     * if user selects car is for sale elsewhere, view.VISIBLE elsewhere warning and views
     *
     * @param seeCarListings - if false, view.GONE elsewhere listing, else view.VISIBLE EditTexts for links
     */
    fun carListingsVisible(seeCarListings: Boolean) {

        if (!seeCarListings)
            _carListingsView.value = View.GONE
        else
            _carListingsView.value = View.VISIBLE
    }

    /**
     * when user selects where car is located (US or CAN), view.VISIBLE proper views
     * parameter true means car is in US
     * parameter false means car is in CAN
     *
     * @param seeCarLocation if true, view.VISIBLE US ZIP view.GONE Canada City/Province, else view.GONE US ZIP view.VISIBLE Canada City/Province
     */
    fun carLocationVisible(seeCarLocation: Boolean) {
        if (seeCarLocation) {
            _carLocationUSView.value = View.VISIBLE
            _carLocationCanView.value = View.GONE
        } else {
            // Invisible, instead of View.GONE so layout margins are maintained
            // for the views below
            _carLocationUSView.value = View.INVISIBLE
            _carLocationCanView.value = View.VISIBLE
        }
        // once either button clicked, make sure to view.GONE error if already displayed
        _carLocError.value = View.GONE
    }

    /**
     *  Determining if modified EditText should be view.VISIBLEn or not
     *
     *  @param carStock if true, view.GONE EditText, else view.VISIBLE EditText
     */
    fun isCarStock(carStock: Boolean) {
        if (carStock)
            _carModifiedView.value = View.GONE
        else
            _carModifiedView.value = View.VISIBLE

        _modGroupError.value = View.GONE
    }

    /**
     * Determine which title spinner to view.VISIBLE
     *
     * @param titleLoc if true, view.VISIBLE US spinner view.GONE CAN spinner, else view.GONE US spinner view.VISIBLE CAN spinner
     */
    fun isTitleInUS(titleLoc: Boolean) {
        if (titleLoc) {
            _titleLocUSView.value = View.VISIBLE
            _titleLocCanView.value = View.GONE
        } else {
            _titleLocUSView.value = View.INVISIBLE
            _titleLocCanView.value = View.VISIBLE
        }
        _titleLocError.value = View.GONE
    }

    /**
     * Determine if other name on title EditText should be view.VISIBLEn
     *
     * @param titleName if true, view.GONE EditText, else view.VISIBLE EditText
     */
    fun isTitleInUserName(titleName: Boolean) {
        if (titleName) {
            _titleNameView.value = View.GONE
        } else
            _titleNameView.value = View.VISIBLE

        _titleNameError.value = View.GONE
    }

    /**
     * Determine whether to view.VISIBLE reserve EditText or not
     *
     * @param wantsReserve if true, view.VISIBLE EditText, else view.GONE EditText
     */
    fun wantAReserve(wantsReserve: Boolean) {
        if (wantsReserve) {
            _reserveView.value = View.VISIBLE
        } else {
            _reserveView.value = View.GONE
        }
        _reserveError.value = View.GONE
    }

    /**
     * On selecting the "Clear All" button in photos
     *
     * After user has entered some photos into the app (from gallery and or from camera),
     * if user taps the "Clear All" button then clear the bitmap array storing all image bitmaps
     * selected so far and display the hint TextView in the Photo RecyclerView
     */
    fun onClrBtnClick() {
        imgBitmaps.clear()
        photoTextVis.value = View.VISIBLE
    }

    /**
     * While the image bitmap array is empty, view.VISIBLE the hint TextView asking user for 8 to 16
     * photos
     */
    fun photoTextVisibility() {
        if (imgBitmaps.isEmpty())
            photoTextVis.value = View.VISIBLE
    }

    /**
     * Function to toggle visibility of Dealer radio error
     */
    fun seeDealerError(see: Boolean) {
        if (see)
            _dealerError.value = View.VISIBLE
        else
            _dealerError.value = View.GONE
    }

    /**
     * Function to toggle visibility of for sale elsewhere radio group error
     * 
     * @param see if true, see the error, else view.GONE
     */
    fun seeSaleError(see: Boolean) {
        if (see)
            _saleError.value = View.VISIBLE
        else
            _saleError.value = View.GONE
    }

    /**
     * Function to toggle visibility of year spinner error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeYearError(see: Boolean) {
        if (see)
            _yearError.value = View.VISIBLE
        else
            _yearError.value = View.GONE
    }

    /**
     * Function to toggle visibility of car location radio group error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeCarLocError(see: Boolean) {
        if (see)
            _carLocError.value = View.VISIBLE
        else
            _carLocError.value = View.GONE
    }

    /**
     * Function to toggle visibility of car mod radio group error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeCarModError(see: Boolean) {
        if (see)
            _modGroupError.value = View.VISIBLE
        else
            _modGroupError.value = View.GONE
    }

    /**
     * Function to toggle visibility of title location radio group error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeTitleLocError(see: Boolean) {
        if (see)
            _titleLocError.value = View.VISIBLE
        else
            _titleLocError.value = View.GONE
    }

    /**
     * Function to toggle visibility of title location spinner error
     * There is one error Textview for both US and CAN spinners
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeTitleLocSpinError(see: Boolean) {
        if (see)
            _titleLocSpinError.value = View.VISIBLE
        else
            _titleLocSpinError.value = View.GONE
    }

    /**
     * Function to toggle visibility of name on title radio group error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeTitleNameError(see: Boolean) {
        if (see)
            _titleNameError.value = View.VISIBLE
        else
            _titleNameError.value = View.GONE
    }

    /**
     * Function to toggle visibility of lienholder radio group error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeLienError(see: Boolean) {
        if (see)
            _lienError.value = View.VISIBLE
        else
            _lienError.value = View.GONE
    }

    /**
     * Function to toggle visibility of title status spinner error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeTitleStatusError(see: Boolean) {
        if (see)
            _titleStatusError.value = View.VISIBLE
        else
            _titleStatusError.value = View.GONE
    }

    /**
     * Function to toggle visibility of reserve radio group error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seeReserveError(see: Boolean) {
        if (see)
            _reserveError.value = View.VISIBLE
        else
            _reserveError.value = View.GONE
    }

    /**
     * Function to toggle visibility of photo error
     *
     * @param see if true, see the error, else view.GONE
     */
    fun seePhotoError(see: Boolean) {
        if (see)
            _photoError.value = View.VISIBLE
        else
            _photoError.value = View.GONE
    }

    /**
     * Figures out the file type of the uri
     *
     * This method takes an image uri, and figures out the file type. In this case, it will mostly be
     * of type jpeg.
     * From: https://www.youtube.com/watch?v=lPfQN-Sfnjw&t=884s
     *
     * @param context the context of the SubmitCarFragment
     * @param uri the image Uri
     * @return the file type of the image uri
     */
    private fun getFileExtension(context: Context, uri: Uri): String {
        val cR = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))!!
    }

    /**
     * This method is responsible for converting images to uri's, sending uri's to storages, and
     * sending all info to firestore
     *
     * uploadInfo converts each image in imgBitmaps array into a uri, and send the uri to google storage.
     * Once photo is sent to google storage, the link to the image is added to imgUrls array so the images
     * can be retrieved later. Then, after all image Urls have been received, combine all info into one object and send to
     * firestore cloud. All of this is done in a coroutine runnning on the I/O dispatcher in the GlobalScope
     *
     * @param context the context of the application, needed for bitmap to uri conversion and Toast
     * @param yourInfo all info from Your Info tab
     * @param carDetails all info from Car Details tab
     * @param titleInfo all info from Title Info tab
     * @param reservePrice the reserve price
     * @param referral the referral information
     */
    fun uploadInfo(
        context: Context,
        yourInfo: YourInfo,
        carDetails: CarDetails,
        titleInfo: TitleInfo,
        reservePrice: ReservePrice,
        referral: Referral
    ) {

        // from documentation
        GlobalScope.launch(Dispatchers.IO) {
            val imgUrls = ArrayList<String>()

            for (photo in 0 until imgBitmaps.size) {

                // convert bitmap to URI
                // https://freakycoder.com/android-notes-72-how-to-convert-bitmap-to-uri-e535391ebdac
                // title is current time so that way pictures do not override each other more than 32 times, which will throw an exception
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    imgBitmaps[photo],
                    System.currentTimeMillis().toString(),
                    null
                )

                val uri = Uri.parse(path)

                // since storageRef points to the "images" folder, this no makes a child of images with a name of currentTimeMills() . file extension
                val fileRef = storageRef.child(
                    System.currentTimeMillis().toString() + "." + getFileExtension(
                        context,
                        uri
                    )
                )

                fileRef.putFile(uri)
                    .addOnSuccessListener {

                        val downloadUri = it.uploadSessionUri.toString()
                        imgUrls.add(downloadUri)

                        // if all images have been uploaded, then
                        // upoad all info to firestore database
                        if (imgUrls.size == imgBitmaps.size) {
                            val allInfo = AllInfo(
                                yourInfo,
                                carDetails,
                                titleInfo,
                                reservePrice,
                                imgUrls,
                                referral
                            )

                            val id = System.currentTimeMillis().toString()

                            // Upload info to database
                            firestore.collection("Submitted Cars")
                                .document(id).set(allInfo)

                            Log.i("SubmitCarFragment", "End of sending to firestore")
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }

            }
        }
    }
}