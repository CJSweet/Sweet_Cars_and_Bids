package com.example.carsandbids.mainpage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
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

    var dummyDisplayCars = ArrayList<Map<String, Any>>()

    lateinit var firestore: FirebaseFirestore

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

        firestore = Firebase.firestore

        getData()

        // Read data from database into list
        Log.i("MainFragment", "There are ${dummyDisplayCars.size} listings in the list")
        // create recyclerview adapter, giving it the list from firestore
        val adapter = ListingsAdapter(this.requireContext(), dummyDisplayCars, this)
        binding.mainRecycler.adapter = adapter
        // set main recyclerview adapter as the one just created

        return binding.root

    }

    fun getData(){
        firestore.collection("Submitted Cars")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.i("MainFragment", document.id + " => " + document.data["yourInfo"])
                        dummyDisplayCars.add(document.data)

                        binding.mainRecycler.adapter!!.notifyItemInserted(dummyDisplayCars.size.minus(1))
                    }
                } else {
                    Log.w("MainViewModel", "Error getting documents.", task.exception)
                }
            })
    }

    override fun onClickCard(listing: Map<String, Any>) {
        val carDetailsMap = listing.get("carDetails") as Map<*, *>
        val make = carDetailsMap["make"].toString()
        Toast.makeText(this.requireContext(), "you clicked the $make", Toast.LENGTH_SHORT).show()

        // val classListing = Listing(listing)

//        val action = MainFragmentDirections.actionMainFragmentToDetailedListingFragment(classListing)
//        NavHostFragment.findNavController(this).navigate(action)
    }
}