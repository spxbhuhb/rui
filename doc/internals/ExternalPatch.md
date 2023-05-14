## External Patch

External patch functions update the external state variables of a component.

They are part of the parent component because they are call-site dependent.
Two different call sites may need two different external patch functions.
In the example below patching the two T1 components requires two different computations.

```kotlin
@Rui
fun test(i: Int) {
  T1(i + 1)
  T1(i + 2)
}
```

The compiler generates a function in the parent component for each call site.

Name of generated external patch functions is `ruiEpX` where `X` is the start offset
of the original function call the external patch belongs to.

```kotlin
fun ruiEp543(it: RuiT1, scopeMask: Long) {
    if ((scopeMask and 1L) == 0) return 0 // 1L is the call site dependency mask

    it as T1
    if (scopeMask and 1L == 0) { // 1L is the mask for `this.value`
        it.p0 = this.value * 2
        it.ruiInvalidate(1) // 1 is the mask of `it.p0`
    }

    return scopeMask // this is a bit more complex for higher order function
}
```