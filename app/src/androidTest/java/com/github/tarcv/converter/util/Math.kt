package com.github.tarcv.converter.util

import kotlin.math.*

class AndroidVector(
    val dx: Float,
    val dy: Float
) {
    val length by lazy {
        sqrt(square(dx) + square(dy))
    }

    val angle by lazy {
        val topAngle = acos(dx / length)
        val rightAngle = asin(-dy / length)
        if (rightAngle >= 0) {
            topAngle
        } else {
            -topAngle
        }
    }

    fun rotateByDegrees(degrees: Float): AndroidVector {
        return if (length < 0.01) { // zero length
            this
        } else {
            val outX = length * cos(angle - degrees * PI / 180.0)
            val outY = -length * sin(angle - degrees * PI / 180.0)
            AndroidVector(outX.toFloat(), outY.toFloat())
        }
    }

    operator fun plus(other: AndroidVector): AndroidVector {
        return AndroidVector(
            dx + other.dx,
            dy + other.dy
        )
    }

    operator fun minus(other: AndroidVector): AndroidVector {
        return AndroidVector(
            dx - other.dx,
            dy - other.dy
        )
    }

    fun toFloatArray(): FloatArray {
        return floatArrayOf(dx, dy)
    }

    companion object {
        private fun square(a: Float) = a * a
    }
}