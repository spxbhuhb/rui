Well, the concept is really simple but the details are really devious. :)

* When you write a function annotated with `@Rui` the compiler plugin creates a class for that function.
* These classes contain the code necessary to update the UI whenever something changes.
* When you build your user interface with these functions, the compiler plugin replaces the function calls with new
  instance creations of these classes.

## Example

```kotlin
fun main() {
    rui {
        page {
            header("Header")
            content("Content")
            footer("Footer")
        }
    }
}

@Rui
fun page(@Rui builder: () -> Unit) {
    builder()
}

@Rui
fun header(title: String) {
    Text(title)
}

@Rui
fun content(content: String) {
    Text(content)
}

@Rui
fun footer(copyright: String) {
    Text(copyright)
}
```

The code above results in 5 classes:

- `RuiRootMain143` - the class generated for the lambda function passed to `rui`
- `RuiPage`
- `RuiHeader`
- `RuiContent`
- `RuiFooter`

Let's have a look at the generated `RuiHeader` class:

```kotlin
class RuiHeader<BT>(
    ruiAdapter: RuiAdapter<BT>,
    ruiParent: RuiFragment<BT>?,
    ruiExternalPatch: (it: RuiFragment<BT>) -> Unit,
    title: String
) : RuiFragment<BT>(
    ruiAdapter,
    ruiParent,
    ruiExternalPatch
) {

    val ruiFragment: RuiFragment<BT>

    var ruiDirty0 = 0  // this bit mask is used to decide what to update

    var title: String = title

    // the functions below manage the lifecycle of the component
    // in a normal component they are quite simple but structurals
    // such as "for","if","when" etc. may have specialized lifecycl

    override fun ruiCreate() {
        fragment.ruiCreate()
    }

    override fun ruiMount(bridge: RuiBridge<BT>) {
        fragment.ruiMount(bridge)
    }

    override fun ruiPatch() {
        fragment.ruiEp344(fragment) // this is an important part!
        fragment.ruiPatch()
    }

    override fun ruiUnmount(bridge: RuiBridge<BT>) {
        fragment.ruiUnmount(bridge)
    }

    override fun ruiDispose() {
        fragment.ruiDispose()
    }

    fun ruiInvalidate0(mask: Int) {
        ruiDirty0 = ruiDirty0 or mask
    }

    override fun ruiPatch() {
        ruiDirty0 = 0
    }

    // These "external patch" functions are responsible for updating
    // the children of this component. These are the real trick behind
    // the reactive behaviour.

    fun ruiEp344(it: RuiText) {
        it as RuiText
        if (ruiDirty0 and 1 != 0) {
            it.content = title
            it.ruiInvalidate0(1)
            it.ruiPatch()
        }
    }

    // Init is used to build the whole component tree. This one is simple
    // but for real pages with hundreds of components it might be very complex.

    init {
        fragment = RuiText(ruiAdapter, this, ::ruiEp344, title)
    }
}
```