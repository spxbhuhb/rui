## Basics: For Loops

You can use the standard Kotlin `for` loops.

```kotlin
@Rui
fun Counter() {
    var count = 0
    Button { "click count: $count" } onClick { count++ }

    for (i in 0 until count) {
        Text("click.")
    }
}
```

## Basics: While Loops

You can use the standard Kotlin `while` and `do-while` loops, but you have to be
careful. If you use a variable that is part of the component state, the while
loop will increase that variable *in the state*.

This means that you have to change that variable to update the content of
the loop and the content will be updated based on the current value of the
variable.

To overcome this, you can use the `stateless` helper function. This function
tells the compiler that the variable is not part of the component state,
it is used only temporarily.

```kotlin
@Rui
fun Counter() {
    var count = 0
    Button { "click count: $count" } onClick { count++ }

    var i = stateless { 0 }

    while (i < count) {
        i++
        Text("click.")
    }
}
```

## Basics: Higher Order Components

Higher order components have a function parameter which in turn is another
component.

```kotlin
@Rui
fun Wrapper(@Rui block: () -> Unit) {
    Text("before the block")
    block()
    Text("after the block")
}

@Rui
fun Counter() {
    var count = 0
    Wrapper {
        Button { "click count: $count" } onClick { count++ }
    }
}
```