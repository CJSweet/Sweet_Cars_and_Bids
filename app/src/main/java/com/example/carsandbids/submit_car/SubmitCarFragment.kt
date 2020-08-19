package com.example.carsandbids.submit_car

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carsandbids.R
import com.example.carsandbids.databinding.SubmitCarBinding
import kotlinx.android.synthetic.main.more_links_edit_text.view.*
import java.util.*
import kotlin.collections.ArrayList


class SubmitCarFragment : Fragment() {

    private lateinit var parentLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding = SubmitCarBinding.inflate(inflater)

        val submitCarViewModel = ViewModelProvider(this).get(SubmitCarViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = submitCarViewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        val arrayListYears = arrayListYears()
//        ArrayAdapter.createFromResource(this, arrayListYears, )


        //bind title status spinner (code based off of spinner documentation: https://developer.android.com/guide/topics/ui/controls/spinner)
        ArrayAdapter.createFromResource(requireActivity().applicationContext, R.array.title_status_array, R.layout.spinner_layout_selected)
            .also { adapter ->
                //Specify layout to use when list of choices appears
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)
                //apply adapter to spinner
                binding.submitTitleStatusSpinner.adapter = adapter
            }

        //bind US state location spinner
        ArrayAdapter.createFromResource(requireActivity().applicationContext, R.array.states_array, R.layout.spinner_layout_selected)
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitTitleLocationUsSpinner.adapter = adapter
            }

        //bind CAN province location spinner
        ArrayAdapter.createFromResource(requireActivity().applicationContext, R.array.can_prov_array, R.layout.spinner_layout_selected)
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitTitleLocationCanSpinner.adapter = adapter
            }

        //bind year spinner adapter
        ArrayAdapter(requireActivity().applicationContext, R.layout.spinner_layout_selected, arrayListYears)
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitYearSpinner.adapter = adapter
            }

        //add more lines for user to enter more links
        // based on code from: https://www.tutorialspoint.com/add-and-remove-views-in-android-dynamically
        parentLayout = binding.submitMoreLinksLayout
        binding.submitMoreListingLinksText.setOnClickListener {
            moreLinks()
        }

        return binding.root
    }

    fun arrayListYears() : ArrayList<Int> {

        val years = ArrayList<Int>()

        for(year in 1980.. Calendar.getInstance().get(Calendar.YEAR)){
            years.add(year)
        }

        return years
    }

    fun moreLinks(){
        val inflater =
            this.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater?
        val newEditView: View = inflater!!.inflate(R.layout.more_links_edit_text, null)

        //get proper dimen value from dimen resource xml
        // from: https://stackoverflow.com/questions/11121028/load-dimension-value-from-res-values-dimension-xml-from-source-code
        val editWidth = resources.getDimension(R.dimen.three_hundred)
        ///resources.displayMetrics.density

        //layout params code inspired by: https://stackoverflow.com/questions/47673723/relative-layout-params-in-kotlin
        var param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(editWidth.toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        param.setMargins(12,6,6,10)
        newEditView.layoutParams = param

        // Add the new row at end of layout
        parentLayout.addView(newEditView, parentLayout.childCount)
    }
}