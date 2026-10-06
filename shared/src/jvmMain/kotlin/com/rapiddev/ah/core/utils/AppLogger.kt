package com.rapiddev.ah.core.utils

/**
 * بديل بسيط لـ android.util.Log
 * يعمل على JVM Desktop بدون أي مكتبة خارجية.
 */
object AppLogger {

    var enabled: Boolean = true

    fun d(tag: String, message: String) {
        if (enabled) println("D/$tag: $message")
    }

    fun w(tag: String, message: String, t: Throwable? = null) {
        if (enabled) {
            println("W/$tag: $message")
            t?.printStackTrace()
        }
    }

    fun e(tag: String, message: String, t: Throwable? = null) {
        if (enabled) {
            System.err.println("E/$tag: $message")
            t?.printStackTrace()
        }
    }
}