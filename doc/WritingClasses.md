## Bridge

When writing a class manually the first thing you have to decide is to write a [bridge](./Internals.md#bridge)
independent or a [bridge](./Internals.md#bridge) dependent class.

All automatically generated classes are bridge independent, that is they can be used with any adapter.

Typically low level classes that interact with the underlying UI are bridge dependent, everything else should
be bridge independent.

In the code the difference is that:

- independents use a type parameter for the bridge
- dependents use the concrete bridge type

## Declarations

- a function with an empty body (this is for the IDE, so you can call the function)
- a class with
    - name of the class is `Rui<name-of-function-first-letter-capitalized>`
    - first 3 parameters are mandatory
    - all other parameters must be the same as the parameters of the function in type and in order

The actual code of the `Text` class which adds a text DOM node. This is a bridge dependent code,
the type of the bridge is `Node` (which is actually a proper DOM node).

```kotlin
@Rui
fun Text(content: String) {
}

class RuiText(
    ruiAdapter: RuiAdapter<Node>,
    ruiParent: RuiFragment<Node>?,
    ruiExternalPatch: (it: RuiFragment<Node>) -> Unit,
    var content: String
) : LeafNode(ruiAdapter, ruiExternalPatch) {

    override val receiver = org.w3c.dom.Text()

    var ruiDirty0 = 0

    override val ruiParent: RuiFragment<Node>
        get() = TODO("Not yet implemented")

    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    override fun ruiCreate() {
        receiver.data = content
    }

    override fun ruiPatch() {
        if (ruiDirty0 and 1 != 0) {
            receiver.data = content
        }
    }

}
```