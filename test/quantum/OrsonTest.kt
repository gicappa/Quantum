package quantum

fun main(args: Array<String>) {
    var a = A()
    var b = B(a)

    a.update("nuovo stato")
    b.printStateA()
}

class A {
    var state: String = ""

    fun update(newState: String) {
        state = newState
        println("State changed in: $state")
    }
}

class B(var a: A) {
    fun printStateA() {
        println("state A ${a.state}")
    }
}

