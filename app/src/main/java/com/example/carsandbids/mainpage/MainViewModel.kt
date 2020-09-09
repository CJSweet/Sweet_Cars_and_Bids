package com.example.carsandbids.mainpage

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carsandbids.DatabaseSingleton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    var carsInDatabase: ArrayList<Map<String, Any>> = ArrayList()

    private var _progressBarView = MutableLiveData<Int>()
    val progressBarView: LiveData<Int>
        get() = _progressBarView

    fun getData() {
        if (carsInDatabase.isEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                DatabaseSingleton.firestore.collection("Submitted Cars")
                    .get()
                    .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.i(
                                    "MainFragment",
                                    document.id + " => " + document.data["yourInfo"]
                                )
                                carsInDatabase.add(document.data)
                            }
                            _progressBarView.value = View.GONE
                            DatabaseSingleton.carsInDatabase = carsInDatabase
                        } else {
                            Log.w("MainViewModel", "Error getting documents.", task.exception)
                        }
                    })
            }
        }
    }
}