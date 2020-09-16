package com.example.carsandbids.detailed_listing

import android.app.ActionBar
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.carsandbids.R
import com.example.carsandbids.databinding.DetailedListingFragmentBinding
import kotlinx.android.synthetic.main.main_listing_tile.view.*

class DetailedListingFragment: Fragment() {

    private lateinit var detailedViewModel: DetailedListingViewModel
    private lateinit var detailedViewModelFactory: DetailedListingViewModelFactory
    private lateinit var binding: DetailedListingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Bind viewmodel and fragment
        binding = DetailedListingFragmentBinding.inflate(inflater)
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

        binding.detailedCurrentPrice.text = Html.fromHtml(detailedViewModel.reservePrice, Html.FROM_HTML_MODE_LEGACY)

        //Set image
        setImage()

        //Set time-price-bids-comments layout
        setTPBC()

        return binding.root
    }

    private fun setImage() {
        val requestOptions: RequestOptions = RequestOptions().placeholder(R.drawable.image_loading_icon1)
            .error(R.drawable.error_image_icon).centerCrop()

        Glide.with(this).load(detailedViewModel.firstImage).apply(requestOptions).into(binding.detailedImageView)
    }

    private fun setTPBC() {
        // Set # of bids (TEMPORARY FOR NOW)
        // TODO: Keep track of how many bids have been placed and comments have been made
        val bids = "<font color=#C1C1C1>Bids</font> <font color=#ffffff>8</font>"
        binding.detailedNumOfBids.text = Html.fromHtml(bids, Html.FROM_HTML_MODE_LEGACY)

        val comments = "<font color=#C1C1C1>Comments</font> <font color=#ffffff>44</font>"
        binding.detailedCommentsNumber.text = Html.fromHtml(comments, Html.FROM_HTML_MODE_LEGACY)
    }
}