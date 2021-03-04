package demo_kotlin

fun printMessage(message: String): Unit {                               // 1
    println(message)
}

fun printMessageWithPrefix(message: String, prefix: String = "Info") {  // 2
    println("[$prefix] $message")
}

fun sum(x: Int, y: Int) {
    x + y
}

fun multiply(x: Int, y: Int) {
    x * y
}
                               // 4