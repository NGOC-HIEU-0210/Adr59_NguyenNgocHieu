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