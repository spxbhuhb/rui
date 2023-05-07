**DISCLAIMER**

Rui is in a **proof-of-concept** phase. The examples below should work
but changes may result in horrendous compilation error messages. Here there be monsters!

To get started with Rui you need a project to work with. Two simple ways to
get one:

* check out the [Example Project](https://github.com/spxbhuhb/rui-example)
* create a new multiplatform project with IDEA and
  * [add the Gradle plugin dependency](../README.md#dependencies)
  * [add the runtime dependency](../README.md#dependencies)

The best place to start experimenting with Rui is `src/jsMain/kotlin/main.kt`:

```kotlin
import hu.simplexion.rui.runtime.dom.RuiDOMAdapter
import hu.simplexion.rui.runtime.dom.html.Button
import hu.simplexion.rui.runtime.dom.html.Text
import hu.simplexion.rui.runtime.rui

fun main() {
  rui(RuiDOMAdapter()) {
    var counter = 0
    Button("Counter: $counter") { counter++ }
    Text("You've clicked $counter times.")
  }
}
```

On JVM you can use `RuiTestAdapter`.

```kotlin
import hu.simplexion.rui.runtime.rui
import hu.simplexion.rui.runtime.testing.RuiTestAdapter
import hu.simplexion.rui.runtime.testing.T1

fun main() {
  rui(RuiTestAdapter()) {
    T1(12)
  }
}
```

## Running

You can use `jsBrowserRun` of the example project. It shows a web page with a functioning
Rui button.

## Adapters

Adapters link Rui components with the underlying UI implementation. For browsers, it can be DOM or Canvas, for JVM
it might be Swing or Android view based.

- `RuiDOMAdapter` is an adapter for web browser DOM.
- `RuiTestAdapter` is an adapter for testing, can be used on any platform.

## Basics: Components

The basic building blocks of a Rui application are components. The
following code defines the "HelloWorld" component which simply displays the
text "Hello World!".

```kotlin
@Rui
fun HelloWorld() {
  Text("Hello World!")
}
```

You can try and add this component to the `main` function.

**IMPORTANT** You have to add `HelloWorld` under `var counter = 0`, more about that below.

```kotlin
fun main() {
  rui(RuiDOMAdapter()) {
    var counter = 0
    HelloWorld()
    Button("Counter: $counter") { counter++ }
    Text("You've clicked $counter times.")
  }
}
```

## Basics: State

Each component has a *state*. When this state changes the component automatically
updates the UI to reflect the change.

Defined variables are part of the component state, and they are *reactive by default*.
We call these *internal state variables*.

```kotlin
@Rui
fun Counter() {
    var counter = 0
    Button { "Click count: $counter" } onClick { counter++ }
}
```

The state of this component consists of the `counter` variable. Whenever you
click on the button, the `counter` is incremented. The component realizes that
the `counter` has been changed and updates the UI.

## Basics: Parameters

You can add parameters to the component. Parameters are parts of the
component state. We call these *external state variables*.

You cannot change the external state variables from the inside of
the component. However, it may happen that the parameter changes
on the outside. In that case the component updates the UI automatically.

```kotlin
@Rui
fun Counter(label: String) {
    var counter = 0
    Button { "$label: $counter" } onClick { counter++ }
}
```

## Basics: Boundary

Rui components have two main areas: *state initialization* and *rendering*.
These are separated by the *boundary*.

Above the boundary, you initialize the component state. This is a one-time
operation, executed when the component is initialized.

Below the boundary, you define how to render the component. This part
is executed whenever the state changes.

**Very important** you cannot define variables, functions etc. in the
*rendering* (except in event handlers, see later). This is a design decision we've
made to avoid confusion. The compiler will report an error if you try to do so.

Rui automatically finds the *boundary*: the first call to another Rui component
function marks the *boundary*.

```kotlin
@Rui
fun Counter() {
    var counter = 0
    // ---- boundary ----
    Button { "Click count: $counter" } onClick { counter++ }
}
```

## Basics: Event Handlers

*Event handlers* are functions called when something happens: user input,
completion of a launched co-routine etc.

Event handlers may change state variables and these changes result in a UI update.

You can define event handlers as local functions or as lambdas. Rui recognizes
when a block changes a state variable and automatically updates the UI.

The handlers in this example are equivalent. Note that whichever button you
click, labels of all show the new counter value.

```kotlin
@Rui
fun Counter() {
    var counter = 0

    fun increment() {
        counter++
    }

    Button { "Click count: $counter" } onClick ::increment
    Button { "Click count: $counter" } onClick { counter++ }
}
```

## Basics: Nesting

Components may contain other components, letting you build complex UI
structures. When `counter` of the parent component changes the child
automatically updates the child component.

```kotlin
@Rui
fun Child(counter: Int) {
    Text("Click count: $counter")
}

@Rui
fun Parent() {
    var counter = 0
    Child(counter) onClick { counter++ }
}
```

## Basics: Conditions

You can use the standard Kotlin `if` and `when` to decide what to display.

```kotlin
@Rui
fun Counter() {
    var count = 0
    Button { "click count: $count" } onClick { count++ }
    if (count < 3) {
        Text("(click count is less than 3)")
    }
}
```

```kotlin
@Rui
fun Counter() {
  var count = 0
  when (count) {
    1 -> Text("click count: 1")
    2 -> Text("click count: 2")
    else -> Text("click count > 2")
  }
  Button { "click to increment" } onClick { count++ }
}
```

## Everthing Else...

...is to be written.

I have actual plans for `for` lops and higher order functions. See  (not 100% up to date):

* [Internals](./Internals.md) for some ideas
* [TestForLoop](../rui-runtime/src/commonTest/kotlin/hu/simplexion/rui/runtime/test/manual/TestForLoop.kt)
* [TestForLoop](../rui-runtime/src/commonTest/kotlin/hu/simplexion/rui/runtime/test/manual/TestHigherOrder.kt)