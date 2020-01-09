package com.github.tarcv.converter

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TemperatureIdentityTest(private val temperature: Float) {
    companion object {
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Array<Float> = arrayOf(
            -100f,
            -1.1f,
            -1f,
            0f,
            1f,
            1.1f,
            32f,
            96f,
            100f
        )
    }

    @Test
    fun fahrenheitIdentityThruCelsius() {
        val celsius = run {
            val converter = Converter(1f)
            converter.fahrenheit = temperature
            converter.celsius
        }
        val newFahrenheit = Converter(celsius).fahrenheit
        Assert.assertEquals(temperature, newFahrenheit, 0.01f)
    }

    @Test
    fun fahrenheitIdentityThruCelsiusWithResetConverter() {
        val celsius = run {
            val converter = Converter(1f)
            converter.fahrenheit = temperature
            converter.celsius
        }
        val newFahrenheit = run {
            val converter = Converter(1f)
            converter.celsius = celsius
            converter.fahrenheit
        }
        Assert.assertEquals(temperature, newFahrenheit, 0.01f)
    }

    @Test
    fun fahrenheitIdentityThruUniversalWithResetConverter() {
        val universal = run {
            val converter = Converter(1f)
            converter.fahrenheit = temperature
            converter.universal
        }
        val newFahrenheit = run {
            val converter = Converter(1f)
            converter.universal = universal
            converter.fahrenheit
        }
        Assert.assertEquals(temperature, newFahrenheit, 0.01f)
    }

    @Test
    fun universalIdentityThruCelsiusWithResetConverter() {
        val celsius = run {
            val converter = Converter(1f)
            converter.universal = temperature
            converter.celsius
        }
        val newUniversal = run {
            val converter = Converter(1f)
            converter.celsius = celsius
            converter.universal
        }
        Assert.assertEquals(temperature, newUniversal, 0.01f)
    }

    @Test
    fun universalIdentityThruFahrenheitWithResetConverter() {
        val fahrenheit = run {
            val converter = Converter(1f)
            converter.universal = temperature
            converter.fahrenheit
        }
        val newUniversal = run {
            val converter = Converter(1f)
            converter.fahrenheit = fahrenheit
            converter.universal
        }
        Assert.assertEquals(temperature, newUniversal, 0.01f)
    }

    @Test
    fun celsiusIdentityThruUniversal() {
        val universal = run {
            val converter = Converter(temperature)
            converter.universal
        }
        val newCelsius = run {
            val converter = Converter(1f)
            converter.universal = universal
            converter.celsius
        }
        Assert.assertEquals(temperature, newCelsius, 0.01f)
    }

    @Test
    fun celsiusIdentityThruUniversalWithResetConverter() {
        val universal = run {
            val converter = Converter(1f)
            converter.celsius = temperature
            converter.universal
        }
        val newCelsius = run {
            val converter = Converter(1f)
            converter.universal = universal
            converter.celsius
        }
        Assert.assertEquals(temperature, newCelsius, 0.01f)
    }

    @Test
    fun celsiusIdentityThruFahrenheit() {
        val fahrenheit = run {
            val converter = Converter(temperature)
            converter.fahrenheit
        }
        val newCelsius = run {
            val converter = Converter(1f)
            converter.fahrenheit = fahrenheit
            converter.celsius
        }
        Assert.assertEquals(temperature, newCelsius, 0.01f)
    }

    @Test
    fun celsiusIdentityThruFahrenheitWithResetConverter() {
        val fahrenheit = run {
            val converter = Converter(1f)
            converter.celsius = temperature
            converter.fahrenheit
        }
        val newCelsius = run {
            val converter = Converter(1f)
            converter.fahrenheit = fahrenheit
            converter.celsius
        }
        Assert.assertEquals(temperature, newCelsius, 0.01f)
    }

    @Test
    fun celsiusCtorVsReset() {
        val ctor = Converter(temperature)
        val reset = run {
            val converter = Converter(1f)
            converter.celsius = temperature
            converter
        }
        Assert.assertEquals(temperature, ctor.celsius, 0.01f)
        Assert.assertEquals(temperature, reset.celsius, 0.01f)

        Assert.assertEquals(ctor.celsius, reset.celsius, 0.01f)
        Assert.assertEquals(ctor.fahrenheit, reset.fahrenheit, 0.01f)
        Assert.assertEquals(ctor.universal, reset.universal, 0.01f)
    }
}