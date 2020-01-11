package com.github.tarcv.converter.test

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.github.tarcv.converter.MainActivity
import com.github.tarcv.converter.R
import com.github.tarcv.converter.screens.MainScreen
import com.github.tarcv.converter.util.accurateClick
import com.github.tarcv.converter.util.withProgress
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*

@RunWith(Parameterized::class)
class ButtonsTest(
    private val buttonLabel: String,
    private val celsiusValue: String,
    private val fahrenheitValue: String
) {
    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    fun testButtonToCelsius() {
        onScreen<MainScreen> {
            temperatureButton(buttonLabel).accurateClick()

            celsiusField.hasText(celsiusValue)
        }
    }

    @Test
    fun testButtonToFahrenheit() {
        onScreen<MainScreen> {
            temperatureButton(buttonLabel).accurateClick()

            fahrenheitField.hasText(fahrenheitValue)
        }
    }

    companion object {
        @Parameterized.Parameters(name = "{0} - {1}C - {2}F")
        @JvmStatic
        fun provider() = arrayOf(
            arrayOf(getString(R.string.absZeroButton), "-273.15", "-459.67"),
            arrayOf(getString(R.string.fahrZeroButton), "-17.78", "0.00"),
            arrayOf(getString(R.string.iceMeltingButton), "0.00", "32.00"),
            arrayOf(getString(R.string.humanBodyButton), "36.50", "96.00"),
            arrayOf(getString(R.string.waterBoilingButton), "100.00", "212.00")
        )

        private fun getString(resId: Int) =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)
    }
}