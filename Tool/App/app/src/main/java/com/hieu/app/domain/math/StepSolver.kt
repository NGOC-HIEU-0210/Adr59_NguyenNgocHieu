package com.hieu.app.domain.math

import com.hieu.app.domain.math.MathFmt.MINUS
import com.hieu.app.domain.math.MathFmt.num
import com.hieu.app.domain.math.MathFmt.paren
import com.hieu.app.domain.math.MathFmt.poly
import com.hieu.app.domain.math.MathFmt.pretty
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

// =================================================================================================
//  Giải toán từng bước.
//  - Hàm y = f(x): giao trục, cực trị (đa thức).
//  - Phương trình theo x (vd x^2 = 4): chuyển vế rồi giải.
//  - Hệ từ 2 hàm số trở lên: lập phương trình hoành độ giao điểm, giải, tìm y.
//  Đa thức giải chính xác (bậc 1, 2, bậc cao bằng nghiệm hữu tỉ + Horner); còn lại giải gần đúng.
// =================================================================================================

data class SolveStep(val heading: String, val lines: List<String>)

data class SolveSection(
    val title: String,
    val steps: List<SolveStep>,
    val conclusion: List<String>
)

data class SolveResult(
    val sections: List<SolveSection>,
    val errors: List<String>
)


fun solveFormulas(formulas: List<String>, vi: Boolean): SolveResult {
    val t = Tr(vi)
    val cleaned = formulas.map { it.trim() }.filter { it.isNotEmpty() }

    val errors = mutableListOf<String>()
    val parsed = mutableListOf<Pair<String, ParsedFormula>>()
    for (raw in cleaned) {
        try {
            parsed += raw to classifyFormula(raw)
        } catch (e: FormulaParseException) {
            errors += "\u201C$raw\u201D: ${e.message}"
        } catch (e: Exception) {
            errors += "\u201C$raw\u201D: " + t("không đọc được biểu thức", "couldn't read this expression")
        }
    }

    val sections = mutableListOf<SolveSection>()

    val functions = parsed.mapNotNull { (raw, f) ->
        (f as? ParsedFormula.AsFunction)?.let { FnItem(raw, it.expr) }
    }

    if (functions.size >= 2) {
        runCatching { buildSystemSection(functions, t) }
            .onSuccess { sections += it }
            .onFailure { errors += t("Không giải được hệ phương trình.", "Couldn't solve the system.") }
    }

    for ((raw, f) in parsed) {
        runCatching {
            when (f) {
                is ParsedFormula.AsFunction -> buildFunctionSection(FnItem(raw, f.expr), t)
                is ParsedFormula.AsEquation -> buildEquationSection(raw, f.left, f.right, t)
            }
        }.onSuccess { sections += it }
            .onFailure { errors += "\u201C$raw\u201D: " + t("không giải được.", "couldn't be solved.") }
    }

    return SolveResult(sections, errors)
}



fun countFunctions(formulas: List<String>): Int =
    formulas.map { it.trim() }.filter { it.isNotEmpty() }.count {
        runCatching { classifyFormula(it) is ParsedFormula.AsFunction }.getOrDefault(false)
    }


fun solveSystem(formulas: List<String>, vi: Boolean): SolveResult {
    val t = Tr(vi)
    val errors = mutableListOf<String>()
    val functions = mutableListOf<FnItem>()
    for (raw in formulas.map { it.trim() }.filter { it.isNotEmpty() }) {
        try {
            val f = classifyFormula(raw)
            if (f is ParsedFormula.AsFunction) functions += FnItem(raw, f.expr)
        } catch (e: FormulaParseException) {
            errors += "\u201C$raw\u201D: ${e.message}"
        } catch (e: Exception) {
            errors += "\u201C$raw\u201D: " + t("không đọc được biểu thức", "couldn't read this expression")
        }
    }
    if (functions.size < 2) return SolveResult(emptyList(), errors)
    val sections = mutableListOf<SolveSection>()
    runCatching { buildSystemSection(functions, t) }
        .onSuccess { sections += it }
        .onFailure { errors += t("Không giải được hệ phương trình.", "Couldn't solve the system.") }
    return SolveResult(sections, errors)
}



fun buildPlotGuide(formulas: List<String>, vi: Boolean): SolveResult {
    val t = Tr(vi)
    val errors = mutableListOf<String>()
    val sections = mutableListOf<SolveSection>()
    for (raw in formulas.map { it.trim() }.filter { it.isNotEmpty() }) {
        try {
            val parsed = classifyFormula(raw)
            if (parsed !is ParsedFormula.AsFunction) {
                errors += "\u201C$raw\u201D: " + t("không phải hàm số y = f(x) nên không có hướng dẫn vẽ.",
                    "isn't a function y = f(x), so there's no plotting guide.")
                continue
            }
            sections += buildPlotSection(FnItem(raw, parsed.expr), t)
        } catch (e: FormulaParseException) {
            errors += "\u201C$raw\u201D: ${e.message}"
        } catch (e: Exception) {
            errors += "\u201C$raw\u201D: " + t("không đọc được biểu thức", "couldn't read this expression")
        }
    }
    return SolveResult(sections, errors)
}

private fun buildPlotSection(f: FnItem, t: Tr): SolveSection {
    val base = buildFunctionSection(f, t)
    val steps = base.steps.toMutableList()
    val p = f.pol

    // Bảng giá trị
    val table = mutableListOf<String>()
    table += t("Chọn vài giá trị x rồi tính y = f(x):", "Pick a few values of x and compute y = f(x):")
    for (xi in -3..3) {
        val x = xi.toDouble()
        val y = f.eval(x)
        table += if (y.isFinite()) "x = ${num(x)}  \u2192  y = ${num(y)}   ${MathFmt.point(x, y)}"
        else "x = ${num(x)}  \u2192  " + t("không xác định", "undefined")
    }
    steps += SolveStep(t("Lập bảng giá trị", "Make a table of values"), table)

    // Cách nối điểm
    val draw = mutableListOf<String>()
    draw += t("Vẽ hệ trục Oxy, chọn đơn vị chia trên hai trục.", "Draw the xy-axes and choose a unit on both axes.")
    draw += t("Đánh dấu các điểm: giao trục, cực trị (nếu có) và các điểm trong bảng.",
        "Mark the points: intercepts, extrema (if any) and the table points.")
    when {
        p != null && p.degree == 1 ->
            draw += t("Đồ thị là một đường thẳng: chỉ cần 2 điểm (ví dụ giao Ox và Oy) rồi kẻ thẳng qua chúng.",
                "The graph is a straight line: two points (e.g. the intercepts) are enough, then draw the line through them.")
        p != null && p.degree == 2 -> {
            val a = p.c[2]
            val axis = -p.c[1] / (2 * a)
            draw += t("Đồ thị là parabol, mở ${if (a > 0) "lên" else "xuống"} vì a = ${num(a)} ${if (a > 0) "> 0" else "< 0"}.",
                "The graph is a parabola opening ${if (a > 0) "upward" else "downward"} because a = ${num(a)} ${if (a > 0) "> 0" else "< 0"}.")
            draw += t("Trục đối xứng: x = ${num(axis)}; các điểm đối xứng qua trục này có cùng tung độ.",
                "Axis of symmetry: x = ${num(axis)}; points symmetric about it share the same y.")
            draw += t("Nối các điểm bằng đường cong trơn qua đỉnh.", "Join the points with a smooth curve through the vertex.")
        }
        p != null ->
            draw += t("Nối các điểm theo thứ tự x tăng dần bằng một đường cong trơn, chú ý chỗ đồ thị đổi chiều ở các cực trị.",
                "Join the points in increasing x with a smooth curve, turning at the extrema.")
        else ->
            draw += t("Nối các điểm bằng đường cong liên tục; không nối qua những giá trị x mà hàm số không xác định.",
                "Join the points with a continuous curve; don't connect across values of x where the function is undefined.")
    }
    steps += SolveStep(t("Vẽ và nối các điểm", "Plot and connect the points"), draw)

    return SolveSection(t("Cách vẽ: ", "How to plot: ") + f.display, steps, base.conclusion)
}

// -------------------------------------------------------------------------------------------------

private class FnItem(val raw: String, val expr: Expr) {
    val pol: Poly? = expr.toPoly()
    val rhsText: String = pretty(raw.substringAfterLast("=").trim())
    fun eval(x: Double) = pol?.eval(x) ?: expr.eval(x)
    /** "y = ..." dạng đẹp. */
    val display: String get() = "y = " + (pol?.let { poly(it) } ?: rhsText)
}

private class Tr(val vi: Boolean) {
    operator fun invoke(v: String, e: String) = if (vi) v else e
}

private sealed interface Sol {
    data object None : Sol
    data object All : Sol
    data class Some(val xs: List<Double>) : Sol
}

private const val EPS = 1e-12
private const val NUM_LO = -30.0
private const val NUM_HI = 30.0
private const val MAX_SHOWN_ROOTS = 12

// =================================================================================================
//  Giải phương trình P(x) = 0
// =================================================================================================

private fun solvePoly(p: Poly, t: Tr, out: MutableList<String>): Sol {
    when (p.degree) {
        0 -> {
            return if (abs(p.c[0]) < EPS) {
                out += t("0 = 0 luôn đúng với mọi x \u2192 phương trình có vô số nghiệm.",
                    "0 = 0 is always true \u2192 infinitely many solutions.")
                Sol.All
            } else {
                out += t("${num(p.c[0])} = 0 là vô lý \u2192 phương trình vô nghiệm.",
                    "${num(p.c[0])} = 0 is false \u2192 no solution.")
                Sol.None
            }
        }

        1 -> {
            val a = p.c[1]
            val b = p.c[0]
            val x = -b / a
            out += t("Phương trình bậc nhất ax + b = 0 với a = ${num(a)}, b = ${num(b)}.",
                "Linear equation ax + b = 0 with a = ${num(a)}, b = ${num(b)}.")
            out += "x = $MINUS" + "b/a = $MINUS(${num(b)})/(${num(a)}) = ${num(x)}"
            return Sol.Some(listOf(x))
        }

        2 -> return solveQuadratic(p, t, out)
    }

    // ---- Bậc >= 3 ----
    if (abs(p.c[0]) < EPS) {
        val q = Poly(p.c.copyOfRange(1, p.c.size))
        out += t("Hệ số tự do bằng 0 \u2192 đặt x làm nhân tử chung: P(x) = x\u00B7Q(x), với Q(x) = ${poly(q)}.",
            "The constant term is 0 \u2192 factor out x: P(x) = x\u00B7Q(x), where Q(x) = ${poly(q)}.")
        out += t("\u2192 x = 0 là một nghiệm. Giải tiếp Q(x) = 0:", "\u2192 x = 0 is a root. Now solve Q(x) = 0:")
        return mergeRoot(0.0, solvePoly(q, t, out))
    }

    val ints = toIntegerCoeffs(p)
    if (ints != null) {
        val a0 = abs(ints.first())
        val an = abs(ints.last())
        val candidates = rationalCandidates(a0, an)
        if (candidates != null) {
            out += t("Thử tìm nghiệm hữu tỉ: nghiệm có dạng p/q, với p là ước của $a0 và q là ước của $an.",
                "Try rational roots: a root has the form p/q, where p divides $a0 and q divides $an.")
            val idx = candidates.indexOfFirst { isRoot(p, it) }
            val r = if (idx >= 0) candidates[idx] else null
            if (r != null) {
                if (idx > 0) {
                    val failed = candidates.take(idx).take(6).joinToString(", ") { "P(${num(it)}) \u2260 0" }
                    out += t("Thử lần lượt: $failed" + (if (idx > 6) ", ..." else "") + " \u2192 chưa phải nghiệm.",
                        "Trying in turn: $failed" + (if (idx > 6) ", ..." else "") + " \u2192 not roots.")
                }
                out += t("Thử x = ${num(r)}: P(${num(r)}) = 0 \u2192 x = ${num(r)} là nghiệm.",
                    "Try x = ${num(r)}: P(${num(r)}) = 0 \u2192 x = ${num(r)} is a root.")
                val (q, _) = p.divideByLinear(r)
                val factor = if (r >= 0) "x $MINUS ${num(r)}" else "x + ${num(-r)}"
                out += t("Chia P(x) cho ($factor) bằng sơ đồ Horner: P(x) = ($factor)\u00B7(${poly(q)}).",
                    "Divide P(x) by ($factor) with Horner's scheme: P(x) = ($factor)\u00B7(${poly(q)}).")
                out += t("Giải tiếp ${poly(q)} = 0:", "Now solve ${poly(q)} = 0:")
                return mergeRoot(r, solvePoly(q, t, out))
            }
            out += t("Không có ước nào thỏa mãn \u2192 đa thức không có nghiệm hữu tỉ.",
                "No candidate works \u2192 the polynomial has no rational root.")
        }
    }

    val bound = 1.0 + p.c.dropLast(1).maxOf { abs(it / p.leadingCoeff()) }
    out += t("Dùng phương pháp số (chia đôi) trên [${num(-bound)}; ${num(bound)}] \u2014 nghiệm gần đúng.",
        "Using a numeric method (bisection) on [${num(-bound)}; ${num(bound)}] \u2014 approximate roots.")
    val roots = numericRoots({ p.eval(it) }, -bound, bound, 20000)
    return if (roots.isEmpty()) {
        out += t("Không tìm thấy nghiệm thực.", "No real root found.")
        Sol.None
    } else {
        out += t("Nghiệm gần đúng: " + roots.joinToString(", ") { "x \u2248 ${num(it)}" },
            "Approximate roots: " + roots.joinToString(", ") { "x \u2248 ${num(it)}" })
        Sol.Some(roots)
    }
}

private fun Poly.leadingCoeff() = c.last()

private fun solveQuadratic(p: Poly, t: Tr, out: MutableList<String>): Sol {
    val a = p.c[2]
    val b = p.c[1]
    val c = p.c[0]
    out += t("Phương trình bậc hai ax\u00B2 + bx + c = 0 với a = ${num(a)}, b = ${num(b)}, c = ${num(c)}.",
        "Quadratic equation ax\u00B2 + bx + c = 0 with a = ${num(a)}, b = ${num(b)}, c = ${num(c)}.")
    val d = b * b - 4 * a * c
    out += "\u0394 = b\u00B2 $MINUS 4ac = ${paren(b)}\u00B2 $MINUS 4\u00B7${paren(a)}\u00B7${paren(c)} = ${num(d)}"
    val tol = 1e-9 * max(1.0, b * b)
    return when {
        d < -tol -> {
            out += t("\u0394 < 0 \u2192 phương trình vô nghiệm (trong số thực).",
                "\u0394 < 0 \u2192 no real solution.")
            Sol.None
        }

        abs(d) <= tol -> {
            val x = -b / (2 * a)
            out += t("\u0394 = 0 \u2192 nghiệm kép: x\u2081 = x\u2082 = $MINUS" + "b/(2a) = ${num(x)}",
                "\u0394 = 0 \u2192 one repeated root: x\u2081 = x\u2082 = $MINUS" + "b/(2a) = ${num(x)}")
            Sol.Some(listOf(x))
        }

        else -> {
            val s = sqrt(d)
            val x1 = (-b - s) / (2 * a)
            val x2 = (-b + s) / (2 * a)
            val (lo, hi) = if (x1 <= x2) x1 to x2 else x2 to x1
            if (MathFmt.isNice(s)) {
                out += t("\u0394 > 0 \u2192 hai nghiệm phân biệt, \u221A\u0394 = ${num(s)}:",
                    "\u0394 > 0 \u2192 two distinct roots, \u221A\u0394 = ${num(s)}:")
                out += "x\u2081 = ($MINUS" + "b $MINUS \u221A\u0394)/(2a) = (${num(-b)} $MINUS ${num(s)})/${paren(2 * a)} = ${num(x1)}"
                out += "x\u2082 = ($MINUS" + "b + \u221A\u0394)/(2a) = (${num(-b)} + ${num(s)})/${paren(2 * a)} = ${num(x2)}"
            } else {
                out += t("\u0394 > 0 \u2192 hai nghiệm phân biệt (\u221A\u0394 không đẹp nên lấy gần đúng):",
                    "\u0394 > 0 \u2192 two distinct roots (\u221A\u0394 is irrational, so approximated):")
                out += "x\u2081 = (${num(-b)} $MINUS \u221A${num(d)})/${paren(2 * a)} \u2248 ${num(x1)}"
                out += "x\u2082 = (${num(-b)} + \u221A${num(d)})/${paren(2 * a)} \u2248 ${num(x2)}"
            }
            Sol.Some(listOf(lo, hi))
        }
    }
}

private fun mergeRoot(r: Double, sub: Sol): Sol = when (sub) {
    is Sol.Some -> Sol.Some(dedupe(sub.xs + r))
    else -> Sol.Some(listOf(r))
}

private fun isRoot(p: Poly, r: Double): Boolean {
    var scale = 0.0
    var pw = 1.0
    for (coef in p.c) {
        scale += abs(coef) * pw
        pw *= abs(r)
    }
    return abs(p.eval(r)) <= 1e-9 * max(1.0, scale)
}

/** Nhân hệ số cho một số nguyên nhỏ để tất cả thành số nguyên. Null nếu không được. */
private fun toIntegerCoeffs(p: Poly): List<Long>? {
    for (m in 1..1000) {
        val scaled = p.c.map { it * m }
        if (scaled.all { abs(it) < 1e9 && abs(it - Math.round(it)) < 1e-7 * max(1.0, abs(it)) }) {
            return scaled.map { Math.round(it) }
        }
    }
    return null
}

private fun divisorsOf(n: Long): List<Long>? {
    if (n <= 0 || n > 2_000_000_000L) return null
    val out = mutableListOf<Long>()
    var i = 1L
    while (i * i <= n) {
        if (n % i == 0L) {
            out += i
            if (i != n / i) out += n / i
        }
        i++
    }
    return out
}

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

private fun rationalCandidates(a0: Long, an: Long): List<Double>? {
    val dp = divisorsOf(a0) ?: return null
    val dq = divisorsOf(an) ?: return null
    if (dp.size.toLong() * dq.size > 5000) return null
    val set = LinkedHashSet<Double>()
    for (p in dp) for (q in dq) if (gcd(p, q) == 1L) {
        set += p.toDouble() / q
        set += -p.toDouble() / q
    }
    return set.sortedWith(compareBy<Double> { abs(it) }.thenByDescending { it })
}

// =================================================================================================
//  Giải gần đúng (bisection)
// =================================================================================================

private fun numericRoots(fn: (Double) -> Double, lo: Double, hi: Double, steps: Int): List<Double> {
    val roots = mutableListOf<Double>()
    val h = (hi - lo) / steps
    var xPrev = lo
    var yPrev = fn(xPrev)
    for (i in 1..steps) {
        val xCur = lo + h * i
        val yCur = fn(xCur)
        if (yPrev.isFinite() && yCur.isFinite()) {
            if (yPrev == 0.0) {
                roots += xPrev
            } else if (yPrev * yCur < 0) {
                var a = xPrev
                var b = xCur
                var fa = yPrev
                repeat(60) {
                    val m = (a + b) / 2
                    val fm = fn(m)
                    if (fa * fm <= 0) b = m else { a = m; fa = fm }
                }
                val r = (a + b) / 2
                // Loại điểm gián đoạn (vd tan(x) đổi dấu qua tiệm cận)
                if (abs(fn(r)) < 1e-6) roots += r
            }
        }
        xPrev = xCur
        yPrev = yCur
    }
    if (yPrev == 0.0) roots += xPrev
    return dedupe(roots)
}

private fun dedupe(values: List<Double>, tol: Double = 1e-7): List<Double> {
    val out = mutableListOf<Double>()
    for (v in values.sorted()) if (out.isEmpty() || abs(v - out.last()) > tol * max(1.0, abs(v))) out += v
    return out
}

/** Giải f(x) = 0 cho hàm bất kỳ (không phải đa thức). */
private fun solveNumeric(fn: (Double) -> Double, t: Tr, out: MutableList<String>): Sol {
    out += t("Đây không phải phương trình đa thức \u2192 giải gần đúng bằng phương pháp chia đôi trong khoảng [${num(NUM_LO)}; ${num(NUM_HI)}].",
        "This isn't a polynomial equation \u2192 solving approximately by bisection on [${num(NUM_LO)}; ${num(NUM_HI)}].")
    var roots = numericRoots(fn, NUM_LO, NUM_HI, 12000)
    if (roots.isEmpty()) {
        out += t("Không tìm thấy nghiệm trong khoảng này.", "No root found in this interval.")
        return Sol.None
    }
    if (roots.size > MAX_SHOWN_ROOTS) {
        out += t("Có nhiều nghiệm, chỉ hiển thị $MAX_SHOWN_ROOTS nghiệm gần gốc tọa độ nhất.",
            "There are many roots; showing only the $MAX_SHOWN_ROOTS closest to the origin.")
        roots = roots.sortedBy { abs(it) }.take(MAX_SHOWN_ROOTS).sorted()
    }
    out += t("Nghiệm gần đúng: " + roots.joinToString(", ") { "x \u2248 ${num(it)}" },
        "Approximate roots: " + roots.joinToString(", ") { "x \u2248 ${num(it)}" })
    return Sol.Some(roots)
}

// =================================================================================================
//  Phần 1: Hàm số y = f(x)
// =================================================================================================

private fun buildFunctionSection(f: FnItem, t: Tr): SolveSection {
    val steps = mutableListOf<SolveStep>()
    val facts = mutableListOf<String>()
    val p = f.pol

    if (p != null) {
        steps += SolveStep(
            t("Tập xác định", "Domain"),
            listOf(t("Hàm đa thức nên xác định với mọi số thực x.", "A polynomial is defined for every real x."))
        )
    }

    // Giao trục Oy
    val y0 = f.eval(0.0)
    val oyLines = mutableListOf<String>()
    if (p != null) {
        oyLines += t("Thay x = 0 vào hàm số: y = ${num(p.c[0])}.", "Substitute x = 0: y = ${num(p.c[0])}.")
        facts += t("Giao trục Oy: ${MathFmt.point(0.0, y0)}", "y-intercept: ${MathFmt.point(0.0, y0)}")
    } else if (y0.isFinite()) {
        oyLines += t("Thay x = 0 vào hàm số: y = ${num(y0)}.", "Substitute x = 0: y = ${num(y0)}.")
        facts += t("Giao trục Oy: ${MathFmt.point(0.0, y0)}", "y-intercept: ${MathFmt.point(0.0, y0)}")
    } else {
        oyLines += t("Hàm số không xác định tại x = 0 \u2192 đồ thị không cắt trục Oy.",
            "The function is undefined at x = 0 \u2192 no y-intercept.")
    }
    steps += SolveStep(t("Giao với trục Oy (x = 0)", "y-intercept (x = 0)"), oyLines)

    // Giao trục Ox
    val oxLines = mutableListOf<String>()
    val sol = if (p != null) {
        oxLines += t("Giải phương trình f(x) = 0:  ${poly(p)} = 0", "Solve f(x) = 0:  ${poly(p)} = 0")
        solvePoly(p, t, oxLines)
    } else {
        oxLines += t("Giải phương trình f(x) = 0.", "Solve f(x) = 0.")
        solveNumeric({ f.expr.eval(it) }, t, oxLines)
    }
    steps += SolveStep(t("Giao với trục Ox (y = 0)", "x-intercepts (y = 0)"), oxLines)
    when (sol) {
        Sol.None -> facts += t("Đồ thị không cắt trục Ox.", "The graph doesn't cross the x-axis.")
        Sol.All -> facts += t("Đồ thị trùng với trục Ox.", "The graph coincides with the x-axis.")
        is Sol.Some -> facts += t("Giao trục Ox: ", "x-intercepts: ") +
            sol.xs.joinToString(", ") { MathFmt.point(it, 0.0) }
    }

    // Cực trị / hệ số góc
    if (p != null) {
        when {
            p.degree == 1 -> {
                val a = p.c[1]
                val dir = if (a > 0) t("đồng biến (đi lên)", "increasing") else t("nghịch biến (đi xuống)", "decreasing")
                steps += SolveStep(
                    t("Hệ số góc", "Slope"),
                    listOf(t("Đường thẳng y = ax + b có a = ${num(a)} ${if (a > 0) "> 0" else "< 0"} \u2192 hàm số $dir trên toàn trục số.",
                        "The line y = ax + b has a = ${num(a)} ${if (a > 0) "> 0" else "< 0"} \u2192 the function is $dir everywhere."))
                )
                facts += t("Hệ số góc a = ${num(a)}: hàm số $dir.", "Slope a = ${num(a)}: the function is $dir.")
            }

            p.degree >= 2 -> addExtremaSteps(p, t, steps, facts)
        }
    }

    return SolveSection(f.display, steps, facts)
}

private fun addExtremaSteps(p: Poly, t: Tr, steps: MutableList<SolveStep>, facts: MutableList<String>) {
    val lines = mutableListOf<String>()
    val d1 = p.derivative()
    val d2 = d1.derivative()
    lines += t("Đạo hàm (quy tắc (x\u207F)' = n\u00B7x\u207F\u207B\u00B9): f'(x) = ${poly(d1)}",
        "Derivative (power rule): f'(x) = ${poly(d1)}")
    lines += t("Giải f'(x) = 0:", "Solve f'(x) = 0:")
    val sol = solvePoly(d1, t, lines)

    val xs = (sol as? Sol.Some)?.xs.orEmpty()
    if (xs.isEmpty()) {
        lines += t("f'(x) = 0 vô nghiệm \u2192 hàm số không có cực trị.", "f'(x) = 0 has no solution \u2192 no extremum.")
        facts += t("Hàm số không có cực trị.", "The function has no extremum.")
    } else {
        lines += t("Xét dấu đạo hàm cấp hai f''(x) = ${poly(d2)}:", "Second derivative test, f''(x) = ${poly(d2)}:")
        for (x0 in xs) {
            val y0 = p.eval(x0)
            val s = d2.eval(x0)
            val kind: String = when {
                s > 1e-9 -> {
                    lines += "f''(${num(x0)}) = ${num(s)} > 0 \u2192 " + t("cực tiểu tại ${MathFmt.point(x0, y0)}", "local minimum at ${MathFmt.point(x0, y0)}")
                    t("Cực tiểu", "Local minimum")
                }

                s < -1e-9 -> {
                    lines += "f''(${num(x0)}) = ${num(s)} < 0 \u2192 " + t("cực đại tại ${MathFmt.point(x0, y0)}", "local maximum at ${MathFmt.point(x0, y0)}")
                    t("Cực đại", "Local maximum")
                }

                else -> {
                    val h = 0.01 * (1 + abs(x0))
                    val before = d1.eval(x0 - h)
                    val after = d1.eval(x0 + h)
                    when {
                        before < 0 && after > 0 -> {
                            lines += "f''(${num(x0)}) = 0 \u2192 " + t("xét dấu f' quanh x = ${num(x0)}: đổi từ \u2212 sang + \u2192 cực tiểu", "f' changes from \u2212 to + \u2192 local minimum")
                            t("Cực tiểu", "Local minimum")
                        }

                        before > 0 && after < 0 -> {
                            lines += "f''(${num(x0)}) = 0 \u2192 " + t("xét dấu f' quanh x = ${num(x0)}: đổi từ + sang \u2212 \u2192 cực đại", "f' changes from + to \u2212 \u2192 local maximum")
                            t("Cực đại", "Local maximum")
                        }

                        else -> {
                            lines += "f''(${num(x0)}) = 0 \u2192 " + t("f' không đổi dấu \u2192 không phải cực trị", "f' doesn't change sign \u2192 not an extremum")
                            ""
                        }
                    }
                }
            }
            if (kind.isNotEmpty()) facts += "$kind: ${MathFmt.point(x0, y0)}"
        }
    }
    steps += SolveStep(t("Cực trị (dùng đạo hàm)", "Extrema (using derivatives)"), lines)
}

// =================================================================================================
//  Phần 2: Phương trình theo x, ví dụ x^2 = 4
// =================================================================================================

private fun buildEquationSection(raw: String, left: Expr, right: Expr, t: Tr): SolveSection {
    val steps = mutableListOf<SolveStep>()
    val facts = mutableListOf<String>()
    val pl = left.toPoly()
    val pr = right.toPoly()
    val parts = raw.split("=")
    val leftText = pretty(parts.first())
    val rightText = pretty(parts.last())

    val moveLines = mutableListOf<String>()
    val diffPoly = if (pl != null && pr != null) pl - pr else null
    if (diffPoly != null) {
        moveLines += "$leftText $MINUS ($rightText) = 0"
        moveLines += "\u2192 ${poly(diffPoly)} = 0"
    } else {
        moveLines += t("Đặt h(x) = vế trái $MINUS vế phải và giải h(x) = 0.", "Let h(x) = left side $MINUS right side and solve h(x) = 0.")
    }
    steps += SolveStep(t("Chuyển vế", "Move everything to one side"), moveLines)

    val solveLines = mutableListOf<String>()
    val sol = if (diffPoly != null) solvePoly(diffPoly, t, solveLines)
    else solveNumeric({ left.eval(it) - right.eval(it) }, t, solveLines)
    steps += SolveStep(t("Giải phương trình", "Solve the equation"), solveLines)

    when (sol) {
        Sol.None -> facts += t("Phương trình vô nghiệm.", "The equation has no solution.")
        Sol.All -> facts += t("Phương trình có vô số nghiệm (đúng với mọi x).", "Infinitely many solutions (true for every x).")
        is Sol.Some -> {
            val check = sol.xs.take(6).map { x ->
                val l = left.eval(x)
                val r = right.eval(x)
                "x = ${num(x)}: " + t("vế trái", "left") + " = ${num(l)}, " + t("vế phải", "right") + " = ${num(r)}" +
                    if (abs(l - r) <= 1e-6 * (1 + abs(l))) t(" \u2192 đúng", " \u2192 correct") else t(" \u2192 sai số", " \u2192 mismatch")
            }
            steps += SolveStep(t("Thử lại nghiệm", "Check the solutions"), check)
            facts += t("Nghiệm: ", "Solution: ") + sol.xs.joinToString("; ") { "x = ${num(it)}" }
        }
    }
    return SolveSection(t("Phương trình: ", "Equation: ") + "$leftText = $rightText", steps, facts)
}

// =================================================================================================
//  Phần 3: Hệ từ 2 hàm số trở lên:  { y = f1(x) ; y = f2(x) ; ... }
// =================================================================================================

private data class Pt(val x: Double, val y: Double)

private fun buildSystemSection(fs: List<FnItem>, t: Tr): SolveSection {
    val n = fs.size
    val steps = mutableListOf<SolveStep>()
    val facts = mutableListOf<String>()
    val a = fs[0]
    val b = fs[1]

    steps += SolveStep(
        t("Hệ phương trình cần giải", "The system to solve"),
        fs.map { "\u2022 ${it.display}" }
    )

    // Bước 1: phương trình hoành độ giao điểm
    val diff = if (a.pol != null && b.pol != null) a.pol - b.pol else null
    val l1 = mutableListOf<String>()
    l1 += t("Hai hàm số cùng bằng y nên tại điểm chung phải có: ${a.fnText()} = ${b.fnText()}",
        "Both expressions equal y, so at a common point: ${a.fnText()} = ${b.fnText()}")
    if (diff != null) {
        l1 += t("Chuyển vế: ${a.fnText()} $MINUS (${b.fnText()}) = 0", "Move to one side: ${a.fnText()} $MINUS (${b.fnText()}) = 0")
        l1 += "\u2192 ${poly(diff)} = 0"
    }
    if (n >= 3) l1 += t("(Hệ có $n hàm số: trước hết tìm điểm chung của hàm thứ 1 và thứ 2, rồi kiểm tra với các hàm còn lại.)",
        "(The system has $n functions: first find the common points of #1 and #2, then check the others.)")
    steps += SolveStep(t("Lập phương trình hoành độ giao điểm", "Set up the intersection equation"), l1)

    // Bước 2: giải
    val l2 = mutableListOf<String>()
    val sol = if (diff != null) solvePoly(diff, t, l2)
    else solveNumeric({ a.expr.eval(it) - b.expr.eval(it) }, t, l2)
    steps += SolveStep(t("Giải phương trình tìm x", "Solve for x"), l2)

    // Bước 3: tìm y, kiểm tra các hàm còn lại
    val candidates = (sol as? Sol.Some)?.xs.orEmpty()
    val common = mutableListOf<Pt>()
    if (candidates.isNotEmpty()) {
        val l3 = mutableListOf<String>()
        for (x in candidates) {
            val y = a.eval(x)
            if (!y.isFinite()) {
                l3 += t("x = ${num(x)}: hàm số không xác định \u2192 loại.", "x = ${num(x)}: undefined \u2192 discarded.")
                continue
            }
            l3 += t("Thay x = ${num(x)} vào ${a.display}: y = ${num(y)}", "Substitute x = ${num(x)} into ${a.display}: y = ${num(y)}")
            var allMatch = true
            for (k in 1 until n) {
                val yk = fs[k].eval(x)
                val ok = yk.isFinite() && abs(yk - y) <= 1e-6 * (1 + abs(y))
                if (k >= 2 || n == 2) {
                    l3 += "   " + t("Kiểm tra với ${fs[k].display}: y = ${num(yk)}", "Check with ${fs[k].display}: y = ${num(yk)}") +
                        if (ok) t(" \u2192 khớp", " \u2192 matches") else t(" \u2192 không khớp", " \u2192 doesn't match")
                }
                if (!ok) allMatch = false
            }
            if (allMatch) common += Pt(x, y)
            else if (n >= 3) l3 += "   " + t("\u2192 điểm này không thuộc mọi đồ thị \u2192 loại.", "\u2192 not on every graph \u2192 discarded.")
        }
        steps += SolveStep(t("Tìm y và kiểm tra", "Find y and verify"), l3)
    }

    // Kết luận
    when {
        sol is Sol.All && n == 2 ->
            facts += t("Hai hàm số trùng nhau \u2192 hệ có vô số nghiệm (mọi điểm trên đồ thị).",
                "The two functions are identical \u2192 infinitely many solutions.")

        sol is Sol.All ->
            facts += t("Hàm 1 và hàm 2 trùng nhau; nghiệm của hệ là các điểm mà các hàm còn lại cũng đi qua.",
                "Functions 1 and 2 are identical; the solutions are the points the remaining functions also pass through.")

        common.isEmpty() -> {
            facts += t("Hệ phương trình vô nghiệm (các đồ thị không có điểm chung).",
                "The system has no solution (the graphs have no common point).")
            if (n == 2 && a.pol?.degree == 1 && b.pol?.degree == 1 && diff != null && diff.degree == 0) {
                facts += t("Hai đường thẳng có cùng hệ số góc nhưng khác tung độ gốc nên song song.",
                    "The two lines have the same slope but different intercepts, so they are parallel.")
            }
            if (n >= 3) {
                val pairs = pairwiseIntersections(fs)
                if (pairs.isNotEmpty()) {
                    facts += t("Tham khảo, giao điểm từng cặp đồ thị:", "For reference, pairwise intersections:")
                    facts += pairs
                }
            }
        }

        else -> facts += t(
            if (common.size == 1) "Hệ có 1 nghiệm (x; y): " else "Hệ có ${common.size} nghiệm (x; y): ",
            if (common.size == 1) "The system has 1 solution (x; y): " else "The system has ${common.size} solutions (x; y): "
        ) + common.joinToString("  ") { MathFmt.point(it.x, it.y) }
    }

    val title = t("Hệ phương trình ($n hàm số)", "System of equations ($n functions)")
    return SolveSection(title, steps, facts)
}

private fun FnItem.fnText(): String = pol?.let { poly(it) } ?: rhsText

private fun pairwiseIntersections(fs: List<FnItem>): List<String> {
    val out = mutableListOf<String>()
    val sink = mutableListOf<String>()
    val dummy = Tr(true)
    for (i in fs.indices) for (j in i + 1 until fs.size) {
        val fi = fs[i]
        val fj = fs[j]
        val diff = if (fi.pol != null && fj.pol != null) fi.pol - fj.pol else null
        val sol = if (diff != null) solvePoly(diff, dummy, sink)
        else Sol.Some(numericRoots({ fi.expr.eval(it) - fj.expr.eval(it) }, NUM_LO, NUM_HI, 12000))
        val pts = (sol as? Sol.Some)?.xs.orEmpty().take(4).mapNotNull { x ->
            val y = fi.eval(x)
            if (y.isFinite()) MathFmt.point(x, y) else null
        }
        val label = "#${i + 1} & #${j + 1}: "
        out += "\u2022 " + label + if (sol is Sol.All) "\u2261" else if (pts.isEmpty()) "\u2014" else pts.joinToString("  ")
    }
    return out
}
