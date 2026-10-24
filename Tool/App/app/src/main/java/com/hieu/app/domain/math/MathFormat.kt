package com.hieu.app.domain.math

import java.util.Locale
import kotlin.math.abs
import kotlin.math.max


object MathFmt {

    const val MINUS = "\u2212" // dấu trừ


    fun fraction(v: Double, maxDen: Int = 24): Pair<Long, Long>? {
        if (!v.isFinite() || abs(v) > 1e12) return null
        val tol = 1e-9 * max(1.0, abs(v))
        val r = Math.round(v)
        if (abs(v - r) < tol) return r to 1L
        for (q in 2..maxDen) {
            val p = Math.round(v * q)
            if (abs(v - p.toDouble() / q) < tol) return p to q.toLong()
        }
        return null
    }


    fun isNice(v: Double): Boolean = fraction(v) != null

    fun num(v: Double): String {
        if (v.isNaN()) return "NaN"
        if (v.isInfinite()) return if (v > 0) "\u221E" else "$MINUS\u221E"
        if (abs(v) < 1e-12) return "0"
        fraction(v)?.let { (p, q) ->
            val body = if (q == 1L) abs(p).toString() else "${abs(p)}/$q"
            return if (p < 0) MINUS + body else body
        }
        val s = if (abs(v) >= 1e9) String.format(Locale.US, "%.3e", v)
        else String.format(Locale.US, "%.4f", v).trimEnd('0').trimEnd('.')
        return s.replace("-", MINUS)
    }


    fun paren(v: Double): String {
        val s = num(v)
        return if (v < 0 || s.contains('/')) "($s)" else s
    }

    fun sup(n: Int): String = when (n) {
        2 -> "\u00B2"
        3 -> "\u00B3"
        else -> "^$n"
    }


    fun poly(p: Poly, v: String = "x"): String {
        val sb = StringBuilder()
        for (i in p.c.indices.reversed()) {
            val a = p.c[i]
            if (abs(a) < 1e-12) continue
            val neg = a < 0
            val absStr = num(abs(a))
            val body = if (i == 0) {
                absStr
            } else {
                val cs = when {
                    abs(abs(a) - 1.0) < 1e-12 -> ""
                    absStr.contains('/') -> "($absStr)"
                    else -> absStr
                }
                cs + v + (if (i >= 2) sup(i) else "")
            }
            if (sb.isEmpty()) {
                if (neg) sb.append(MINUS)
                sb.append(body)
            } else {
                sb.append(if (neg) " $MINUS " else " + ").append(body)
            }
        }
        return if (sb.isEmpty()) "0" else sb.toString()
    }

    fun point(x: Double, y: Double): String = "(${num(x)}; ${num(y)})"


    fun pretty(raw: String): String =
        raw.trim()
            .replace(Regex("\\s+"), " ")
            .replace(" - ", " $MINUS ")
            .replace("^2", "\u00B2")
            .replace("^3", "\u00B3")
            .replace("*", "\u00B7")
            .replace("sqrt(", "\u221A(")
}
