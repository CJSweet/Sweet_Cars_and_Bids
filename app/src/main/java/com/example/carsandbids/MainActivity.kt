package com.example.carsandbids

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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