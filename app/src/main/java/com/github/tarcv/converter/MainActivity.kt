package com.github.tarcv.converter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val normalCelsius = 20
    private val maximumCelsius = 150
    val maximumUniversal = Converter(maximumCelsius.toFloat()).universal.toInt()
    private val converter = Converter(normalCelsius.toFloat())
    private val buttonUniversals = LinkedHashMap<Int, Int>()
    private var autoChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSeekbar()

        initCelsiusField()

        initFahrenheitField()

        val fahrenheitZero = Converter(1f).apply {
            fahrenheit = 0f
        }.universal
        val iceMelting = Converter(0f).universal
        val humanBody = Converter(37f).universal
        val waterBoiling = Converter(100f).universal

        addButton(R.string.absZeroButton, 0f)
        addButton(R.string.fahrZeroButton, fahrenheitZero)
        addButton(R.string.iceMeltingButton, iceMelting)
        addButton(R.string.humanBodyButton, humanBody)
        addButton(R.string.waterBoilingButton, waterBoiling)

        var lastKnownSeekBarWidth = -1
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val actualSeekBarWidth = seekBar.width -
                    seekBar.paddingLeft - seekBar.paddingRight
            if (actualSeekBarWidth != lastKnownSeekBarWidth) {
                lastKnownSeekBarWidth = actualSeekBarWidth

                var belowSeekBar = true

                buttonUniversals.entries.forEach { (id, universal) ->
                    belowSeekBar = !belowSeekBar

                    val constraintSet = ConstraintSet()
                    constraintSet.clone(rootLayout)
                    constraintSet.clear(id, ConstraintSet.LEFT)
                    constraintSet.clear(id, ConstraintSet.TOP)
                    constraintSet.clear(id, ConstraintSet.BOTTOM)

                    val button = findViewById<Button>(id)
                    val thumbCenter =
                        (actualSeekBarWidth * universal.toFloat() / maximumUniversal).toInt() +
                                seekBar.paddingLeft
                    constraintSet.connect(
                        id, ConstraintSet.LEFT, seekBar.id, ConstraintSet.LEFT,
                        thumbCenter
                    )
                    if (belowSeekBar) {
                        constraintSet.connect(
                            id, ConstraintSet.TOP,
                            belowSpace.id, ConstraintSet.BOTTOM
                        )
                        constraintSet.applyTo(rootLayout)
                        button.translationX = (button.measuredHeight / 2).toFloat()
                        button.pivotX = 0f
                        button.pivotY = 0f
                        button.rotation = 90f
                    } else {
                        constraintSet.connect(
                            id, ConstraintSet.BOTTOM,
                            aboveSpace.id, ConstraintSet.TOP
                        )
                        constraintSet.applyTo(rootLayout)
                        button.translationX = (-button.measuredWidth - button.measuredHeight / 2).toFloat()
                        button.pivotX = button.measuredWidth.toFloat()
                        button.pivotY = button.measuredHeight.toFloat()
                        button.rotation = 90f
                    }
                }
            }
        }
    }

    private fun addButton(titleResId: Int, universal: Float) {
        val button = Button(this)
        button.id = View.generateViewId()
        button.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        button.text = getString(titleResId)
        button.setOnClickListener {
            val celsius = Converter(1f).let {
                it.universal = universal
                it.celsius
            }
            celsiusText.setText(floatToField(celsius))
        }

        rootLayout.addView(button)
        buttonUniversals[button.id] = universal.toInt()
    }

    private fun initFahrenheitField() {
        fahrenheitText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!autoChange) {
                    updateTextFields {
                        fieldToFloat(s)?.let {
                            converter.fahrenheit = it
                            celsiusText.setText(floatToField(converter.celsius))
                            seekBar.progress = converter.universal.toInt()
                        }
                    }
                }
            }
        })
    }

    private fun initCelsiusField() {
        celsiusText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!autoChange) {
                    updateTextFields {
                        fieldToFloat(s)?.let {
                            converter.celsius = it
                            fahrenheitText.setText(floatToField(converter.fahrenheit))
                            seekBar.progress = converter.universal.toInt()
                        }
                    }
                }
            }
        })
    }

    private fun initSeekbar() {
        seekBar.progress = Converter(normalCelsius.toFloat()).universal.toInt()
        seekBar.max = maximumUniversal
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    converter.universal = progress.toFloat()
                    updateTextFields {
                        celsiusText.setText(floatToField(converter.celsius))
                        fahrenheitText.setText(floatToField(converter.fahrenheit))
                    }
                }
            }
        })
    }

    private fun floatToField(float: Float) = String.format("%.2f", float)

    private fun fieldToFloat(s: CharSequence) = s.toString().removePrefix("+").toFloatOrNull()

    private fun updateTextFields(block: () -> Unit) {
        if (autoChange) {
            throw AssertionError()
        }
        autoChange = true
        block()
        autoChange = false
    }
}

