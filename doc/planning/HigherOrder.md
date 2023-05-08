**NOTE** This document is a bit confused at the moment as it is my working scratch or something like that.

This is rather complex because of the scopes involved. Unfortunately we can't build on the actual Kotlin scopes because
we transform a temporary function call structure into a permanent object instance tree.

Handling of higher order functions may be very complex as the example below shows.

```kotlin
@Rui
fun ho(ph: Int, @Rui func: (pc: Int) -> Unit) {
    val vv = randomInt()
    func(ph * 2 + vv)
    func(ph * 3 + vv)
}

@Rui
fun test(p0: Int) {
    ho(p0) { p1 ->
        ho(p1) { p2 ->
            ho(p2) { p3 ->
                T1(p0 + p1 + p2 + p3)
            }
        }
    }
}
```

Consider the followings:

1. Higher-order components may perform calculations needed to properly patch the components deeper in the tree.
2. Components lower in the tree may access state variables higher in the tree.
3. A higher-order component may use the parameter function more than once.

The fragment tree that represents the example above:

```text
 ID    Fragment      External State   Internal State     External State Value
 
  0    Test          p0                                  p0
  1      Ho          ph               1.vv               p0
  2        Ho        ph               2.vv               p0 * 2 + 1.vv
  3          Ho      ph               3.vv               (p0 * 2 + 1.vv) * 2 + 2.vv
  4            T1    v                                   ((p0 * 2 + 1.vv) * 2 + 2.vv) * 2 + 3.vv
  5          Ho      ph               5.vv               (p0 * 2 + 1.vv) * 2 + 2.vv
  6            T1    v                                   ((p0 * 2 + 1.vv) * 2 + 2.vv) * 2 + 5.vv 
  7        Ho        ph               7.vv               p0 * 2 + 1.vv     
  8          Ho      ph               8.vv               (p0 * 2 + 1.vv) * 2 + 7.vv 
  9            T1    v                                   ((p0 * 2 + 1.vv) * 2 + 7.vv) * 2 + 8.vv 
 10          Ho      ph              10.vv               (p0 * 2 + 1.vv) * 2 + 7.vv
 11            T1    v                                   ((p0 * 2 + 1.vv) * 2 + 7.vv) * 2 + 10.vv               
```

This tree is actually quite comfortable and non-problematic. Any state variable changes result in changes in every
fragment down the tree.

However, if we consider this example (note that `ho` just passes the value:

```kotlin
@Rui
fun ho1(ph: Int, @Rui func: (pc: Int) -> Unit) {
    val v = 1
    ho2(ph + v)
}

@Rui
fun ho2(ph: Int, @Rui func: (pc: Int) -> Unit) {
    func(ph)
}

@Rui
fun test(p0: Int) {
    ho1(p0) { p1 ->
        ho2(p1) { p2 ->
            T1(p0)
        }
    }
}
```

This case is quite problematic. What happens when I change `ho1.v`?

I'm thinking about introducing a `RuiBuilder` class which would be responsible for making components by calling the
builder function:

```kotlin
class RuiBuilder<BT, CT : RuiFragment<BT>>(scope: CT, builder: () -> RuiFragment<BT>) {

    fun buildFragment(): RuiFragment {

    }
}
```

The key insight here is that the parameter functions cannot extend the state. As they are part of the rendering,
they cannot define new state variables, therefore the structure of the state we are working here is static,
it is defined by the parameters of the parameter functions.

The parameter functions implicitly define components with:

- an external state, defined by the parameters of the parameter function (empty when there are no parameters),
- an empty internal state.

We use classes from the runtime to create instances of these implicit components:

- RuiImplicit0
- RuiImplicit1
- RuiImplicitN

The number in the class name is the number of state variables the class stores. The first two should cover
most of the use cases why the last may be used for any number of parameters.

To access the state stored in implicit instances we use the `RuiFragment.ruiParent` property.

Example:

**These examples are not in sync with the current code.**

```kotlin
@Rui
fun h0(ph: Int, @Rui func: (pc: Int) -> Unit) {
    func(ph * 2)
}

@Rui
fun test(value: Int) {
    ho(value) { p1 ->
        T1(value + p1)
    }
}
```

```kotlin
class RuiImplicit1<BT, VT, FT : RuiFragment<BT>>(
    override val ruiParent: RuiFragment<BT>,
    override val ruiAdapter: RuiAdapter<BT>,
    override val ruiExternalPatch: (it: RuiFragment<BT>) -> Unit,
    var v0: VT
) : RuiFragment {

    var inner: FT? = null

    override fun ruiPatch() {
        inner?.let {
            it.ruiExternalPatch()
            it.ruiPatch()
        }
    }
}
```

```kotlin
class RuiHo<BT>(
    override val ruiParent: RuiFragment<BT>,
    override val ruiAdapter: RuiAdapter<BT>,
    override val ruiExternalPatch: (it: RuiFragment<BT>) -> Unit,
    var ph: Int,
    var builder: (syn: RuiImplicit1<BT, Int, *>) -> RuiFragment<BT>
) : RuiFragment<BT> {

    val fragment: RuiFragment<BT>

    fun ruiGep0(it: RuiFragment<BT>) {
        it.v0 = this.ph0 * 2
    }

    init {
        fragment = RuiImplicit1(
            ruiAdapter,
            ::ruiGep0,
            this.ph0 * 2
        ).also {
            inner = builder(it)
        }
    }
}
```

```kotlin
class RuiTest<BT>(
    override val ruiAdapter: RuiAdapter<BT>
) : RuiGeneratedFragment<BT> {

    override val ruiParent: RuiFragment<BT>? = null
    override val ruiExternalPatch: (it: RuiFragment<BT>) -> Unit = { }

    override val fragment: RuiFragment<BT>

    var value: Int = 1

    fun ruiEp123(it: RuiH0) {
        it.ph = this.value
    }

    fun ruiEp456(it: RuiT1) {
        it.p0 = this.value + it.scope.v1
    }

    fun ruiBuilder1(syn: RuiImplicit1<BT, Int, *>): RuiFragment<BT> =
        RuiT1(ruiAdapter, ::ruiGep123, this.value + syn.v1)

    init {
        fragment = RuiHo(ruiAdapter, ::ruiGep456, ::ruiBuilder1, p0)
    }
}
```
