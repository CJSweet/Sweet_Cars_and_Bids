package com.example.carsandbids

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.example.carsandbids.submit_car.AllInfo
import com.example.carsandbids.submit_car.CarDetails
import com.example.carsandbids.submit_car.TitleInfo
import com.example.carsandbids.submit_car.YourInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object DatabaseSingleton {
    val firestore : FirebaseFirestore = Firebase.firestore

    private var storageRef: StorageReference = FirebaseStorage.getInstance().getReference("images")

    var carsInDatabase : ArrayList<Map<String, Any>> = ArrayList()

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
     * sendInfo converts each image in imgBitmaps array into a uri, and send the uri to google storage.
     * Once photo is sent to google storage, the link to the image is added to imgUrls array so the images
     * can be retrieved later. Then, after all image Urls have been received, combine all info into one object and send to
     * firestore cloud. All of this is done in a coroutine running on the I/O dispatcher in the GlobalScope
     *
     * @param context the context of the application, needed for bitmap to uri conversion and Toast
     * @param yourInfo all info from Your Info tab
     * @param carDetails all info from Car Details tab
     * @param titleInfo all info from Title Info tab
     * @param reservePrice the reserve price
     * @param referral the referral information
     * @param imgBitmaps the img Bitmap array from SubmitCarViewModel, that will be converted to URI and sent to storage
     */
    fun sendInfo(
        context: Context,
        yourInfo: YourInfo,
        carDetails: CarDetails,
        titleInfo: TitleInfo,
        reservePrice: String,
        referral: String,
        imgBitmaps: ArrayList<Bitmap>
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
                    .addOnSuccessListener { it ->
                        it.storage.downloadUrl.addOnCompleteListener { task ->
                            imgUrls.add(task.result.toString())

                            val timestamp = System.currentTimeMillis()

                            val id = timestamp.toString()

                            // if all images have been uploaded, then
                            // upload all info to firestore database
                            if (imgUrls.size == imgBitmaps.size) {
                                val allInfo = AllInfo(
                                    timestamp,
                                    yourInfo,
                                    carDetails,
                                    titleInfo,
                                    reservePrice,
                                    imgUrls,
                                    referral
                                )

                                // Upload info to database
                                firestore.collection("Submitted Cars")
                                    .document(id).set(allInfo)

                                Log.i("SubmitCarFragment", "End of sending to firestore")
                            }
                        }

                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener {
                        it.result
                    }
            }
        }
    }

}