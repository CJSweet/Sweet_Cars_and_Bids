package com.example.carsandbids.submit_car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.carsandbids.databinding.SubmitCarBinding

class SubmitCarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = SubmitCarBinding.inflate(inflater)

        return binding.root
    }
}