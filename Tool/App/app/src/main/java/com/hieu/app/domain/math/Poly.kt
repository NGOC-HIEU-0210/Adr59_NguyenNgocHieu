package com.hieu.app.domain.math

import kotlin.math.abs
import kotlin.math.max


class Poly(coeffs: DoubleArray) {

    val c: DoubleArray = normalize(coeffs)

    val degree: Int get() = c.size - 1
    val isZero: Boolean get() = c.size == 1 && c[0] == 0.0

    operator fun plus(o: Poly): Poly {
        val n = max(c.size, o.c.size)
        return Poly(DoubleArray(n) { c.getOrElse(it) { 0.0 } + o.c.getOrElse(it) { 0.0 } })
    }

    operator fun minus(o: Poly): Poly {
        val n = max(c.size, o.c.size)
        return Poly(DoubleArray(n) { c.getOrElse(it) { 0.0 } - o.c.getOrElse(it) { 0.0 } })
    }

    operator fun unaryMinus(): Poly = Poly(DoubleArray(c.size) { -c[it] })

    operator fun times(o: Poly): Poly {
        val r = DoubleArray(c.size + o.c.size - 1)
        for (i in c.indices) for (j in o.c.indices) r[i + j] += c[i] * o.c[j]
        return Poly(r)
    }

    fun pow(n: Int): Poly {
        var r = Poly(doubleArrayOf(1.0))
        repeat(n) { r = r * this }
        return r
    }

    fun scale(k: Double): Poly = Poly(DoubleArray(c.size) { c[it] * k })


    fun eval(x: Double): Double {
        var r = 0.0
        for (i in c.indices.reversed()) r = r * x + c[i]
        return r
    }

    fun derivative(): Poly =
        if (c.size == 1) Poly(doubleArrayOf(0.0))
        else Poly(DoubleArray(c.size - 1) { c[it + 1] * (it + 1) })


    fun divideByLinear(r: Double): Pair<Poly, Double> {
        val n = c.size
        if (n == 1) return Poly(doubleArrayOf(0.0)) to c[0]
        val q = DoubleArray(n - 1)
        var carry = c[n - 1]
        q[n - 2] = carry
        for (i in n - 2 downTo 1) {
            carry = c[i] + carry * r
            q[i - 1] = carry
        }
        return Poly(q) to (c[0] + carry * r)
    }

    companion object {
        fun constant(v: Double) = Poly(doubleArrayOf(v))

        private fun normalize(src: DoubleArray): DoubleArray {
            if (src.isEmpty()) return doubleArrayOf(0.0)
            val maxAbs = src.maxOf { abs(it) }
            val eps = 1e-12 * max(1.0, maxAbs)
            var n = src.size
            while (n > 1 && abs(src[n - 1]) <= eps) n--
            val out = src.copyOf(n)
            if (n == 1 && abs(out[0]) <= eps) out[0] = 0.0
            return out
        }
    }
}
