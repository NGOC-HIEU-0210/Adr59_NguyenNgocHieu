package com.hieu.app.domain.math


sealed class Expr {
    abstract fun eval(x: Double): Double
}

private data class Num(val v: Double) : Expr() {
    override fun eval(x: Double) = v
}

private object VarX : Expr() {
    override fun eval(x: Double) = x
}

private object ConstPi : Expr() {
    override fun eval(x: Double) = Math.PI
}

private data class Neg(val e: Expr) : Expr() {
    override fun eval(x: Double) = -e.eval(x)
}

private data class BinOp(val op: Char, val l: Expr, val r: Expr) : Expr() {
    override fun eval(x: Double): Double {
        val a = l.eval(x)
        val b = r.eval(x)
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            '^' -> Math.pow(a, b)
            else -> Double.NaN
        }
    }
}

private data class Call(val fn: String, val arg: Expr) : Expr() {
    override fun eval(x: Double): Double {
        val a = arg.eval(x)
        return when (fn) {
            "sin" -> Math.sin(a)
            "cos" -> Math.cos(a)
            "tan" -> Math.tan(a)
            "sqrt" -> Math.sqrt(a)
            "log" -> Math.log10(a)
            "ln" -> Math.log(a)
            "abs" -> Math.abs(a)
            else -> Double.NaN
        }
    }
}

class FormulaParseException(message: String) : Exception(message)

fun parseFormula(raw: String): Expr = FormulaParser(raw).parseAll()

private val FUNCS = listOf("sqrt", "log", "sin", "cos", "tan", "abs", "ln")

private class FormulaParser(input: String) {
    private val tokens: List<String> = tokenize(preprocess(input))
    private var pos = 0

    fun parseAll(): Expr {
        if (tokens.isEmpty()) throw FormulaParseException("Biểu thức trống")
        val e = parseExpr()
        if (pos != tokens.size) throw FormulaParseException("Không đọc được biểu thức")
        return e
    }

    private fun peek(): String? = tokens.getOrNull(pos)
    private fun consume(): String = tokens[pos++]

    private fun parseExpr(): Expr {
        var node = parseTerm()
        while (true) {
            val t = peek()
            if (t == "+" || t == "-") {
                consume()
                node = BinOp(t[0], node, parseTerm())
            } else break
        }
        return node
    }

    private fun parseTerm(): Expr {
        var node = parseUnary()
        while (true) {
            val t = peek()
            if (t == "*" || t == "/") {
                consume()
                node = BinOp(t[0], node, parseUnary())
            } else break
        }
        return node
    }

    private fun parseUnary(): Expr {
        val t = peek()
        if (t == "-") { consume(); return Neg(parseUnary()) }
        if (t == "+") { consume(); return parseUnary() }
        return parsePower()
    }

    private fun parsePower(): Expr {
        val base = parsePrimary()
        if (peek() == "^") {
            consume()
            // right-associative
            return BinOp('^', base, parseUnary())
        }
        return base
    }

    private fun parsePrimary(): Expr {
        val t = peek() ?: throw FormulaParseException("Biểu thức chưa hoàn chỉnh")
        if (t == "(") {
            consume()
            val e = parseExpr()
            if (peek() != ")") throw FormulaParseException("Thiếu dấu ')'")
            consume()
            return e
        }
        if (t == "x") { consume(); return VarX }
        if (t == "pi") { consume(); return ConstPi }
        if (t in FUNCS) {
            consume()
            if (peek() != "(") throw FormulaParseException("Thiếu '(' sau '$t'")
            consume()
            val arg = parseExpr()
            if (peek() != ")") throw FormulaParseException("Thiếu dấu ')'")
            consume()
            return Call(t, arg)
        }
        val num = t.toDoubleOrNull()
        if (num != null) { consume(); return Num(num) }
        throw FormulaParseException("Không hiểu ký hiệu '$t'")
    }
}

private fun preprocess(input: String): String {
    var s = input.trim().lowercase().replace(Regex("\\s+"), "")
    if (s.contains("=")) s = s.substringAfterLast("=")
    return s
}

/** Turns the cleaned string into tokens, inserting implicit '*' where a human would mean multiplication. */
private fun tokenize(s: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    val funcsByLength = FUNCS.sortedByDescending { it.length }

    fun lastIsValueEnd(): Boolean {
        val last = tokens.lastOrNull() ?: return false
        return last == ")" || last == "x" || last == "pi" || last.toDoubleOrNull() != null
    }

    while (i < s.length) {
        val c = s[i]
        when {
            c.isDigit() || c == '.' -> {
                val start = i
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                if (lastIsValueEnd()) tokens.add("*")
                tokens.add(s.substring(start, i))
            }
            c == 'x' -> {
                if (lastIsValueEnd()) tokens.add("*")
                tokens.add("x"); i++
            }
            c == '(' -> {
                if (lastIsValueEnd()) tokens.add("*")
                tokens.add("("); i++
            }
            c == ')' -> { tokens.add(")"); i++ }
            c in "+-*/^" -> { tokens.add(c.toString()); i++ }
            else -> {
                var matched = false
                for (f in funcsByLength) {
                    if (s.startsWith(f, i)) {
                        if (lastIsValueEnd()) tokens.add("*")
                        tokens.add(f); i += f.length; matched = true; break
                    }
                }
                if (!matched && s.startsWith("pi", i)) {
                    if (lastIsValueEnd()) tokens.add("*")
                    tokens.add("pi"); i += 2; matched = true
                }
                if (!matched) throw FormulaParseException("Ký tự không hợp lệ: '$c'")
            }
        }
    }
    return tokens
}


// ---------------------------------------------------------------------------------------------
// Phân loại công thức + trích đa thức (dùng cho phần giải toán từng bước)
// ---------------------------------------------------------------------------------------------

sealed interface ParsedFormula {

    data class AsFunction(val expr: Expr) : ParsedFormula

    data class AsEquation(val left: Expr, val right: Expr) : ParsedFormula
}

private val FUNCTION_LHS = Regex("^(y|[a-z]\\(x\\))$")

fun classifyFormula(raw: String): ParsedFormula {
    val s = raw.trim().lowercase().replace(Regex("\\s+"), "")
    val parts = s.split("=")
    if (parts.size != 2) return ParsedFormula.AsFunction(parseFormula(raw))
    val (left, right) = parts
    if (left.isEmpty() || FUNCTION_LHS.matches(left)) {
        return ParsedFormula.AsFunction(parseFormula(right))
    }
    return ParsedFormula.AsEquation(parseFormula(left), parseFormula(right))
}

/** Giá trị hằng nếu biểu thức không chứa x, ngược lại null. */
private fun Expr.constValue(): Double? = when (this) {
    is Num -> v
    is ConstPi -> Math.PI
    is VarX -> null
    is Neg -> e.constValue()?.let { -it }
    is BinOp -> if (l.constValue() != null && r.constValue() != null) eval(0.0) else null
    is Call -> if (arg.constValue() != null) eval(0.0) else null
}

/**
 * Nếu biểu thức là đa thức theo x thì trả về đa thức đó, ngược lại null
 * (ví dụ có sin(x), sqrt(x), 2^x, chia cho x...).
 */
fun Expr.toPoly(): Poly? {
    val p = toPolyOrNull() ?: return null
    return if (p.c.all { it.isFinite() }) p else null
}

private fun Expr.toPolyOrNull(): Poly? = when (this) {
    is Num -> Poly.constant(v)
    is ConstPi -> Poly.constant(Math.PI)
    is VarX -> Poly(doubleArrayOf(0.0, 1.0))
    is Neg -> e.toPolyOrNull()?.let { -it }
    is Call -> if (arg.constValue() != null) Poly.constant(eval(0.0)) else null
    is BinOp -> {
        val a = l.toPolyOrNull()
        val b = r.toPolyOrNull()
        when (op) {
            '+' -> if (a != null && b != null) a + b else null
            '-' -> if (a != null && b != null) a - b else null
            '*' -> if (a != null && b != null) a * b else null
            '/' -> {
                val d = r.constValue()
                if (a != null && d != null && d != 0.0) a.scale(1.0 / d) else null
            }
            '^' -> {
                val n = r.constValue()
                if (a != null && n != null && n >= 0.0 && n == Math.floor(n) && n <= 12.0) a.pow(n.toInt()) else null
            }
            else -> null
        }
    }
}
