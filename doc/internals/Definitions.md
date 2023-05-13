## Definitions

#### component

A *component* is a class that extends `RuiFragment` and has its own *component state*. The
state may be empty. The compiler plugin generates components from *original functions*.
It is also possible to manually write a component, actually most low-level components
that interact with the underlying UI are written manually.

#### original function

An *original function* is a function the UI programmer writes and is annotated with `@Rui`.
The compiler plugin turns original functions into a *components*.

#### structural

A *structural* is a general building block used by *components* to build the structure of
the rendered UI. These are pre-defined by the Rui runtime: `RuiBlock`, `RuiWhen`,
`RuiLoop`. Structurals do not have a state, they use the state of the component they are
defined in when deciding how to render the UI.

#### fragment

A *fragment* is a *component* or a *structural*. Fragments are the building blocks of
the runtime Rui structure.

#### higher order function

A *higher-order function* is an *original function* that has a function type parameter
annotated with `@Rui`.

#### parameter function

A *parameter function* is a function passed as parameter to a *higher-order function*.

#### scope

### state variable

A *state variable* is a property of a *component*, part of the *component state*.

#### external state variable

An *external state variable* is a *state variable* that is derived from a parameter of
the *original function*.

#### internal state variable

An *internal state variable* is a *state variable* that is derived from a variable defined
in the body of the *original function*.

#### runtime

The *runtime* is the `hu.simplexion.zakadabar:rui-runime` module. This contains base
classes and interfaces. It is important that this is a "static" runtime, there is no
engine running in the background to perform updates.

