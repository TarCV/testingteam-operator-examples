package com.github.tarcv.converter

import org.junit.Assert
import org.junit.Test

class ConverterTest {
    @Test
    fun universalZeroIsAbsoluteZero() {
        val converter = Converter(1f)
        converter.universal = 0f
        Assert.assertEquals(-273.15f, converter.celsius, 0.01f)
    }

    @Test
    fun absoluteZeroIsUniversalZero() {
        val converter = Converter(-273.15f)
        Assert.assertEquals(0f, converter.universal, 0.01f)
    }

    @Test
    fun iceMelting() {
        val converter = Converter(0f)
        Assert.assertEquals(32f, converter.fahrenheit, 0.01f)
    }

    @Test
    fun bodyTemperature() {
        val converter = Converter(37f)
        Assert.assertEquals(98.6f, converter.fahrenheit, 0.01f)
    }

    @Test
    fun waterBoiling() {
        val converter = Converter(100f)
        Assert.assertEquals(212f, converter.fahrenheit, 0.01f)
    }

    @Test
    fun fahrenheitIsIntegerInUniversal() {
        val converter = Converter(1f)
        converter.fahrenheit = 1f
        Assert.assertEquals(0f, converter.universal - converter.universal.toInt(), 0.01f)
    }

    @Test
    fun celsiusIsIntegerInUniversal() {
        val converter = Converter(1f)
        Assert.assertEquals(0f, converter.universal - converter.universal.toInt(), 0.01f)
    }
}
