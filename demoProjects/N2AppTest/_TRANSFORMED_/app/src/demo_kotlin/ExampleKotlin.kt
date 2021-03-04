package demo_kotlin

import uminho.di.greenlab.trepnlibrary.TrepnLib
import com.hunter.library.debug

@HunterDebug
fun printMessage(message: String): Unit {
    TrepnLib.traceMethod("demo_kotlin->printMessage|-891985903")
    println(message)
}

@HunterDebug
fun printMessageWithPrefix(message: String, prefix: String = "Info") {
    TrepnLib.traceMethod("demo_kotlin->printMessageWithPrefix|996978530")
    println("[$prefix] $message")
}

@HunterDebug
fun sum(x: Int, y: Int) {
    TrepnLib.traceMethod("demo_kotlin->sum|-1183758944")
    x + y
}

@HunterDebug
fun multiply(x: Int, y: Int) {
    TrepnLib.traceMethod("demo_kotlin->multiply|-1183758944")
    x * y
}
