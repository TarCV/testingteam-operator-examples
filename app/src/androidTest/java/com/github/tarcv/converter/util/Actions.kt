package com.github.tarcv.converter.util

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers
import com.agoda.kakao.progress.KSeekBar
import com.agoda.kakao.text.KButton
import org.hamcrest.Matcher
import kotlin.math.abs

fun KButton.accurateClick() {
    this.act {
        object : ViewAction {
            private val defaultClick = GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val screenTopLeft = run {
                        val location = IntArray(2)
                        view.getLocationOnScreen(location)
                        AndroidVector(location[0].toFloat(), location[1].toFloat())
                    }

                    val viewPivotToTopLeft = AndroidVector(
                        0 - view.pivotX,
                        0 - view.pivotY
                    )
                    val screenPivotToTopLeft = viewPivotToTopLeft.rotateByDegrees(view.rotation)
                    val screenPivot = screenTopLeft - screenPivotToTopLeft

                    val viewPivotToCenter = AndroidVector(
                        view.width / 2 - view.pivotX,
                        view.height / 2 - view.pivotY
                    )
                    val screenPivotToCenter = viewPivotToCenter.rotateByDegrees(view.rotation)
                    val screenCenter = screenPivot + screenPivotToCenter

                    screenCenter.toFloatArray()
                },
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY
            )

            override fun getDescription(): String = defaultClick.description

            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isDisplayingAtLeast(50)
            }

            override fun perform(uiController: UiController, view: View) {
                defaultClick.perform(uiController, view)
            }
        }
    }
}

fun KSeekBar.withProgress(function: (Int) -> Unit) {
    this.act {
        object : ViewAction {
            override fun getDescription(): String = "Do something with progress value"

            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(SeekBar::class.java)
            }

            override fun perform(uiController: UiController, view: View) {
                val seekBar = view as SeekBar
                function(seekBar.progress)
            }
        }
    }
}