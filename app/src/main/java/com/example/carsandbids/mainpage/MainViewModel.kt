package com.example.carsandbids.mainpage

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainViewModel : ViewModel() {

    // Declaring a firestore reference to upload all data
    private lateinit var firestore: FirebaseFirestore

    init {
        firestore = Firebase.firestore
    }

    fun fetchData(){
        firestore.collection("Submitted Cars")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d("MainViewModel", document.id + " => " + document.data["yourInfo"])
                        val infoMap = document.data["yourInfo"] as Map<*,*>
                        val nameuser = infoMap["name"].toString()
                        Log.d("MainViewModel", nameuser)
                    }
                } else {
                    Log.w("MainViewModel", "Error getting documents.", task.exception)
                }
            })

    }
}