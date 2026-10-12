package com.urikai.app.game

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

fun degToRad(degrees: Double): Double = degrees * PI / 180.0

/**
 * `String.format` の共通コード版（Web では java.lang.String.format が使えない）。
 * 対応: `%%` `%s` `%d` `%f`、フラグ `,` `+` `0` `-`、幅、精度（`%.1f`）。
 */
fun String.fmt(vararg args: Any?): String {
    val out = StringBuilder()
    var argIndex = 0
    var i = 0
    while (i < length) {
        val c = this[i]
        if (c != '%') {
            out.append(c)
            i++
            continue
        }
        i++
        require(i < length) { "Dangling % in \"$this\"" }
        if (this[i] == '%') {
            out.append('%')
            i++
            continue
        }
        var grouping = false
        var plus = false
        var zeroPad = false
        var leftAlign = false
        while (i < length && this[i] in ",+0-") {
            when (this[i]) {
                ',' -> grouping = true
                '+' -> plus = true
                '0' -> zeroPad = true
                '-' -> leftAlign = true
            }
            i++
        }
        var width = 0
        while (i < length && this[i].isDigit()) {
            width = width * 10 + (this[i] - '0')
            i++
        }
        var precision = -1
        if (i < length && this[i] == '.') {
            i++
            precision = 0
            while (i < length && this[i].isDigit()) {
                precision = precision * 10 + (this[i] - '0')
                i++
            }
        }
        require(i < length) { "Bad format \"$this\"" }
        val conv = this[i]
        i++
        val arg = args[argIndex++]
        val body = when (conv) {
            's' -> arg.toString()
            'd' -> formatInteger((arg as Number).toLong(), grouping, plus)
            'f' -> formatFixed((arg as Number).toDouble(), if (precision < 0) 6 else precision, grouping, plus)
            else -> error("Unsupported conversion %$conv in \"$this\"")
        }
        out.append(pad(body, width, zeroPad && !leftAlign && conv != 's', leftAlign))
    }
    return out.toString()
}

private fun pad(body: String, width: Int, zeroPad: Boolean, leftAlign: Boolean): String {
    if (body.length >= width) return body
    val fill = width - body.length
    return when {
        leftAlign -> body + " ".repeat(fill)
        zeroPad -> {
            val signLen = if (body.startsWith("-") || body.startsWith("+")) 1 else 0
            body.substring(0, signLen) + "0".repeat(fill) + body.substring(signLen)
        }
        else -> " ".repeat(fill) + body
    }
}

private fun groupDigits(digits: String): String {
    val sb = StringBuilder()
    val lead = digits.length % 3
    for ((idx, ch) in digits.withIndex()) {
        if (idx > 0 && (idx - lead) % 3 == 0) sb.append(',')
        sb.append(ch)
    }
    return sb.toString()
}

private fun formatInteger(value: Long, grouping: Boolean, plus: Boolean): String {
    val negative = value < 0
    val digits = if (value == Long.MIN_VALUE) "9223372036854775808" else abs(value).toString()
    val body = if (grouping) groupDigits(digits) else digits
    return when {
        negative -> "-$body"
        plus -> "+$body"
        else -> body
    }
}

private fun formatFixed(value: Double, precision: Int, grouping: Boolean, plus: Boolean): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
    val scale = 10.0.pow(precision)
    // 1e-9 で 0.05→0.0999.. のような二進誤差を吸収して四捨五入（Java の HALF_UP 相当）
    val scaled = floor(abs(value) * scale + 0.5 + 1e-9).toLong()
    val intPart = scaled / scale.toLong()
    val fracPart = scaled % scale.toLong()
    val intDigits = intPart.toString()
    val intText = if (grouping) groupDigits(intDigits) else intDigits
    val text = if (precision == 0) intText else intText + "." + fracPart.toString().padStart(precision, '0')
    val negative = value < 0 && scaled != 0L
    return when {
        negative -> "-$text"
        plus -> "+$text"
        else -> text
    }
}
