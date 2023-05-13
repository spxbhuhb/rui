## Higher Order Functions

Transformation of higher order functions is rather complex because of the scopes involved. Unfortunately we can't
build on the actual Kotlin scopes (we transform a temporary runtime memory stack into a persistent object instance
tree).

The following example shows the complexity:

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

### Scopes

*start scope* State of the component in which the *parameter function* is defined.

*local scope* The state defined by the parameters of one *parameter function*.

*end scope* The state that is defined as the union of the *start scope* and all *local scopes*
between the *start scope* and the *end scope* (inclusive).

Example:

```kotlin
fun start(s1: Int) {      // start scope
    var s2: Int
    ho(s1) { p1 ->         // local scope
        ho(s2) { p2 ->     // end scope
            t1(p1 + p2)
        }
    }
}

fun ho(ho1: Int, @Rui func: (pc: Int) -> Unit) {
    var ho2: Int
}
```

States:

```
start scope    local scope    end scope

    s1             s1            s1
    s2             s2            s2
                   p1            p1
                                 p2
```

### Considerations

1. Higher-order components may perform calculations needed to properly patch the components deeper in the tree.
2. Components lower in the tree may access function parameters higher in the tree.
3. A higher-order component may use the parameter function more than once.

### Implicit Components

The parameter functions implicitly define components with the *end scope* as the component state.
We use classes from the runtime to create instances of these implicit components:

- RuiImplicit0
- RuiImplicit1
- RuiImplicitN

The number in the class name is the number of state variables the class stores. The first two should cover
most of the use cases why the last may be used for any number of parameters.

These classes **do not actually store** the state variables from the *start scope* and the intermediate *local scopes*.
Instead, they have a `ruiScope` property which stores the first upper level scope.

### Patching

Effective patching have to decide which components may be ignored. External patch passed to implicit fragments have to
know the dirty mask of their parent scopes to make these decisions possible.

This code has a limitation of 64 state variables, but I think we can live with that.

```kotlin
fun ruiEp123(it: RuiFragment, scopeMask: Long): Long {
    it as RuiImplicit
    if ((scopeMask and it.callSiteDependencyMask) == 0) return 0
    // do external patch stuff, updates it.ruiDirty0
    return scopeMask or (it.ruiDirty0 lsh numberOfStateVariables)
}

fun ruiPatch(scopeMask: Long) {
    val extendedScopeMask = fragment.ruiEp123(scopeMask)
    if (extendedScopeMask != 0) fragment.patch(extendedScopeMask)
}
```

`callSiteDependencyMask` bits belong to the state variables of the *end scope*. Any given call site will probably use
a subset of the state variables. We do not have to patch components that do not depend on changed variables.

`numberOfStateVariables` is the number of state variables in the parent scope.

For normal components (not higher order) `scopeMask` is simply the `ruiDirty0` of the start scope.

### Recognizing

On the call site we cannot see if the argument is annotated with `Rui` or not. Therefore, we have to fetch the
definition of the component called to see if this is actually a higher order function or not.

Compiler version: 1.8.21

```kotlin
@Rui
fun a(@Rui stuff: () -> Unit) {
    stuff()
}

@Rui
fun b() {
    a { T0() }
}
```

```text
MODULE_FRAGMENT name:<main>
  FILE fqName:hu.simplexion.rui.kotlin.plugin.adhoc fileName:/Users/tiz/src/rui/rui-kotlin-plugin/src/test/kotlin/hu/simplexion/rui/kotlin/plugin/adhoc/Adhoc.kt
    FUN name:a visibility:public modality:FINAL <> (stuff:kotlin.Function0<kotlin.Unit>) returnType:kotlin.Unit
      annotations:
        Rui
      VALUE_PARAMETER name:stuff index:0 type:kotlin.Function0<kotlin.Unit>
        annotations:
          Rui
      BLOCK_BODY
        CALL 'public abstract fun invoke (): R of kotlin.Function0 [operator] declared in kotlin.Function0' type=kotlin.Unit origin=INVOKE
          $this: GET_VAR 'stuff: kotlin.Function0<kotlin.Unit> declared in hu.simplexion.rui.kotlin.plugin.adhoc.a' type=kotlin.Function0<kotlin.Unit> origin=VARIABLE_AS_FUNCTION
    FUN name:b visibility:public modality:FINAL <> () returnType:kotlin.Unit
      annotations:
        Rui
      BLOCK_BODY
        CALL 'public final fun a (stuff: kotlin.Function0<kotlin.Unit>): kotlin.Unit declared in hu.simplexion.rui.kotlin.plugin.adhoc' type=kotlin.Unit origin=null
          stuff: FUN_EXPR type=kotlin.Function0<kotlin.Unit> origin=LAMBDA
            FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> () returnType:kotlin.Unit
              BLOCK_BODY
                CALL 'public final fun T0 (): kotlin.Unit declared in hu.simplexion.rui.runtime.testing.FragmentsKt' type=kotlin.Unit origin=null
```