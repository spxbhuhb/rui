Transforms are compiler add-ons that enrich the compilation of a Rui component some way.

## CSS Transform

For JavaScript target the CSS transform converts transformations into CSS classes or inline CSS styles on the component.

The following example instructs the compiler plugin to create a CSS class named "rui-a" and assign that class to the
component when created if the component supports the CSS transform.

```kotlin
@Rui
fun a() {
    marginBottom = 10.px
}
```

## Internals

Transforms are classes that implement the `RuiTransform` interface. When the compiler plugin compiles a class
it recognizes when a value of a transform is set. For example:

```kotlin
@Rui
fun a() {
    exampleKey = "someValue"
}
```

```kotlin
class ExampleTransform : RuiTransform {

    var ruiTransformEntries = mutableMapOf<String, String>()

    fun ruiBuildTransform() {

    }

    var exampleKey: String = ""
}
```

