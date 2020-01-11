package com.github.tarcv.converter.screens

import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.progress.KSeekBar
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.text.KButton
import com.github.tarcv.converter.R

class MainScreen: Screen<MainScreen>() {
    val celsiusField = KEditText { withId(R.id.celsiusText) }
    val fahrenheitField = KEditText { withId(R.id.fahrenheitText) }
    val seekBar = KSeekBar { withId(R.id.seekBar) }

    fun temperatureButton(label: String): KButton = KButton { withText(label) }
}