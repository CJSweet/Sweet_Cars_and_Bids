package com.example.carsandbids.detailed_listing

import android.app.ActionBar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.carsandbids.R
import com.example.carsandbids.databinding.DetailedListingFragmentBinding

class DetailedListingFragment: Fragment() {

    private lateinit var detailedViewModel: DetailedListingViewModel
    private lateinit var detailedViewModelFactory: DetailedListingViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DetailedListingFragmentBinding.inflate(inflater)
        detailedViewModelFactory = DetailedListingViewModelFactory(DetailedListingFragmentArgs.fromBundle(requireArguments()).position)
        detailedViewModel = ViewModelProvider(this, detailedViewModelFactory).get(DetailedListingViewModel::class.java)
        binding.viewModel = detailedViewModel

        binding.lifecycleOwner = viewLifecycleOwner

        binding.detailedMileage.text = getString(R.string.detailed_mileage, detailedViewModel.mileage)

        binding.detailedMods.text = getString(R.string.detailed_mods, detailedViewModel.mods)

        binding.detailedSellerName.text = getString(R.string.detailed_seller_info, detailedViewModel.sellerName)

        binding.detailedSellerType.text = getString(R.string.detailed_seller_type, detailedViewModel.sellerType)

        binding.detailedTitleStatus.text = getString(R.string.detailed_title_status, detailedViewModel.titleStatus)

        binding.detailedTitleLocation.text = getString(R.string.detailed_title_location, detailedViewModel.titleLocation)

        val requestOptions: RequestOptions = RequestOptions().placeholder(R.drawable.image_loading_icon1)
            .error(R.drawable.error_image_icon).centerCrop()

        Glide.with(this).load(detailedViewModel.firstImage).apply(requestOptions).into(binding.detailedImageView)

        return binding.root
    }
}