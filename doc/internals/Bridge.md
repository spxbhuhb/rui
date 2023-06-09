## Bridge

The bridge connects Rui fragments with their representation in the underlying
UI. Low-level fragments (those that directly interact with the actual UI)
typically implement the `RuiBridge` interface and transform their internal
state into an actual UI state.

The `BT` type parameter of the bridge is a type in the underlying UI, typically
ancestor of all UI elements, such as `Node` in HTML or `View` in Android.

`ruiMount` and `ruiUnmount` functions get the bridge of the parent fragment
and use `add` and `remove` methods to add and remove themselves. Some bridges
also implement the `replace` method which makes it possible to replace a
fragment with another in place. This is used by `if` and `when`.

### Bridge dependent and bridge independent fragments

A bridge independent fragment is one that does not depend on the actual type
its bridge uses. On the other hand, a bridge dependent fragment is one
that uses the bridge in some very specific manner.

For example `RuiBlock` is a bridge independent fragment. It does not care
about what goes on, it just has a few children, and they will handle the
bridging themselves.

`Text` fragments are typically bridge dependent because each platform has its
own way to add constant text to the UI. In browsers for example you use
`document.createTextNode`.

Bridge independent fragments use type parameter for the bridge receiver type:

```kotlin
open class RuiBlock<BT>(
    override val ruiAdapter: RuiAdapter<BT>,
    vararg val fragments: RuiFragment<BT>
) : RuiFragment<BT> {
    // ...
}
```

Fragments generated by the plugin are bridge independent, thus usable with any
kind of adapter/bridge.