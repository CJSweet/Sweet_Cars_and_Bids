package com.example.carsandbids.detailed_listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class DetailedListingViewModelFactory(private val position: Int): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailedListingViewModel::class.java)){
            return DetailedListingViewModel(position) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}