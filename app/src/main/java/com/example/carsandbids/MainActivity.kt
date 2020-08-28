package com.example.carsandbids

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.carsandbids.mainpage.MainFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //set up bot nav bar with nav_graph, got code from: https://www.youtube.com/watch?v=pT_4rV3gO78
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bot_nav_bar)
        val navController = findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }

    //for the ability to remove focus from editText after tapping away
    // code from: https://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside/28939113#28939113
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        //if the action is a press
        if (event.action == MotionEvent.ACTION_DOWN) {
            //set a value to a view that is the currentFocus
            val v = currentFocus
            //if the current view is an EditText
            if (v is EditText) {
                //get coordinates of editext
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                //if not tapping in the editext that has focus..
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    //..then clear focus and hide keyboard
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}