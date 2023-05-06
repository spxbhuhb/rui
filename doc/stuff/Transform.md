**IMPORTANT** This is an idea I've been kicking around. It is not implemented and maybe never will be.

## Transforms: Introduction

Transforms are little helper functions that let you change the state of a
component with clear, easy to read code.

There are two types of transforms: outer and inner. For both the transformed
component has to support the transformation (more about that later).

This outer transform sets the `name` state variable of `Wrapper` to "Wrapped Block".

```kotlin
@Rui
fun Use() {
    Wrapper {  } name "Wrapped Block"
}
```

This inner transform sets the `name` state variable of `Wrapper` to "Wrapped block".

```kotlin
@Rui
fun Use() {
    Wrapper {
        name = "Wrapped Block"
        Button { "click count: $count" } onClick { count++ }
    }
}
```

You do not have to write any transforms, they are purely for convenience.
That said, most components in the library provide these because we like
convenience.

## Transforms: Outer

The definition of an outer transform is a bit of cumbersome, worth it only when
you use the component many times.

`CompTransformScope` is the object we use to scope the transforms into this
component. You don't have to worry about it much, it is used only to
indicate which type of component are we transforming.

To write an outer transform for a component:

1. create a transform scope object (`CompTransformScope`)
2. return with this object from the component function
3. define the transform function (`CompTransformScope.value`)

```kotlin
@Rui
fun Comp(): CompTransformScope {
    var name = "block"
    Text("the name is $name")
    return CompTransformScope
}

object CompTransformScope

@Transform
infix fun CompTransformScope.value(v: String) = Unit
```

## Transforms: Inner

For higher order functions you can define inner transforms by defining the
block passed as receiver function on a transform object.

These let you set state variables of the higher order function inside the block
passed as parameter.

To make an inner transform for a component:

1. create a transform scope object (`CompTransformScope`)
2. use the scope as the receiver of the parameter function (`block`)
3. add the transformed variable into the transform scope (`CompTransformScope.name`)

```kotlin
@Rui
fun Comp(@Rui block: CompTransformScope.() -> Unit) {
    var name = "block"
    Text("before the $block")
    CompTransformScope.block()
    Text("after the $block")
}

@Transform
object CompTransformScope {
    var name: String = ""
}
```