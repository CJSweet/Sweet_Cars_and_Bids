package com.example.carsandbids.detailed_listing

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.carsandbids.R
import com.example.carsandbids.databinding.DetailedListingFragmentBinding

class DetailedListingFragment: Fragment() {

    private lateinit var detailedViewModel: DetailedListingViewModel
    private lateinit var binding: DetailedListingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Bind ViewModel and fragment
        binding = DetailedListingFragmentBinding.inflate(inflater)
        val detailedViewModelFactory = DetailedListingViewModelFactory(DetailedListingFragmentArgs.fromBundle(requireArguments()).position)
        detailedViewModel = ViewModelProvider(this, detailedViewModelFactory).get(DetailedListingViewModel::class.java)
        binding.viewModel = detailedViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set toolbar
        val toolbar : Toolbar = binding.detailedToolbar as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Cars & Bids"
        toolbar.setNavigationIcon(R.drawable.back_up_arrow)
        toolbar.setNavigationOnClickListener {
            val action = DetailedListingFragmentDirections.actionDetailedListingFragmentToMainFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        // Set all dynamic textViews
        setTextViews()

        // Set image
        setImageRecycler()

        return binding.root
    }

    private fun setTextViews() {

        binding.detailedMileage.text = Html.fromHtml(detailedViewModel.mileage, Html.FROM_HTML_MODE_LEGACY)

        binding.detailedMods.text = Html.fromHtml(detailedViewModel.stock, Html.FROM_HTML_MODE_LEGACY)

        binding.detailedSellerName.text = Html.fromHtml(detailedViewModel.sellerName, Html.FROM_HTML_MODE_LEGACY)

        binding.detailedSellerType.text = Html.fromHtml(detailedViewModel.sellerType, Html.FROM_HTML_MODE_LEGACY)

        binding.detailedTitleStatus.text = Html.fromHtml(detailedViewModel.titleStatus, Html.FROM_HTML_MODE_LEGACY)

        binding.detailedTitleLocation.text = Html.fromHtml(detailedViewModel.titleLocation, Html.FROM_HTML_MODE_LEGACY)

        binding.detailedCurrentPrice.text = Html.fromHtml(detailedViewModel.reservePrice, Html.FROM_HTML_MODE_LEGACY)

        // Set # of bids (TEMPORARY FOR NOW)
        // TODO: Keep track of how many bids have been placed and comments have been made
        val bids = "<font color=#C1C1C1>Bids</font> <font color=#ffffff>8</font>"
        binding.detailedNumOfBids.text = Html.fromHtml(bids, Html.FROM_HTML_MODE_LEGACY)

        val comments = "<font color=#C1C1C1>Comments</font> <font color=#ffffff>44</font>"
        binding.detailedCommentsNumber.text = Html.fromHtml(comments, Html.FROM_HTML_MODE_LEGACY)
    }

    private fun setImageRecycler() {
        // set adapter
        val adapter = ImageAdapter(detailedViewModel.imageArray)
        // set SnapHelper to snap to each photo
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.detailedImageRecycler)
        binding.detailedImageRecycler.adapter = adapter
    }

}