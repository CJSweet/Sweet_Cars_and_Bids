package com.example.carsandbids.mainpage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.carsandbids.DatabaseSingleton
import com.example.carsandbids.databinding.MainFragmentBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//TODO: Make sure both the listings page and the sell car page have the same
//  margins, to keep the user familiar

class MainFragment : Fragment(), ListingsAdapter.OnClickCardListener {

    lateinit var binding: MainFragmentBinding

    lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Bind data, inflate, get viewmodel, and lifecycleOwner
        binding = MainFragmentBinding.inflate(inflater)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set Toolbar
        val toolbar : Toolbar? = binding.mainToolbar as Toolbar

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Cars & Bids"

        //set viewModel array
        mainViewModel.getData()

        // Read data from database into list
        Log.i("MainFragment", "There are ${mainViewModel.carsInDatabase.size} listings in the list")
        // create recyclerview adapter, giving it the list from firestore
        val adapter = ListingsAdapter(this.requireContext(), mainViewModel.carsInDatabase, this)
        binding.mainRecycler.adapter = adapter
        // set main recyclerview adapter as the one just created

        return binding.root

    }

    override fun onClickCard(position: Int) {
        val action = MainFragmentDirections.actionMainFragmentToDetailedListingFragment(position)
        NavHostFragment.findNavController(this).navigate(action)
    }
}