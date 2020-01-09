package com.github.tarcv.converter

class Converter(
    private var _celsius: Float
) {
    private val absoluteZeroCelsius = -273.15
    private val celsiusToSeekbar = 180

    private var _fahrenheit: Float = celsiusToFahrenheit(_celsius)
    private var _universal: Float = celsiusToUniversal(_celsius)

    var celsius: Float
        get() = _celsius
        set(value) {
            _fahrenheit = celsiusToFahrenheit(value)
            _universal = celsiusToUniversal(value)

            _celsius = value
        }

    var fahrenheit: Float
        get() = _fahrenheit
        set(value) {
            _celsius = (value - 32f) * 5 / 9
            _universal = ((value - 32f) * 5 * celsiusToSeekbar / 9 - (absoluteZeroCelsius * celsiusToSeekbar)).toFloat()

            _fahrenheit = value
        }

    var universal: Float
        get() = _universal
        set(value) {
            _celsius = ((value / celsiusToSeekbar) + absoluteZeroCelsius).toFloat()
            _fahrenheit = celsiusToFahrenheit(_celsius)

            _universal = value
        }

    private fun celsiusToFahrenheit(celsius: Float): Float = (celsius * 9 / 5 + 32f)

    private fun celsiusToUniversal(celsius: Float): Float {
        return ((celsius - absoluteZeroCelsius) * celsiusToSeekbar).toFloat()
    }
}