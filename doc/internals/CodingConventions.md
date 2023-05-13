## Coding Conventions

Plugin source code function names follow these conventions:

- `init*` functions are called during instance initialization
- `build*` functions are called from `RuiClassBuilder.build` (maybe deeper in the chain), they do no return with
  anything
- `ir*` functions create and return with `IrElement` instances
- `trace*` functions add trace code when it is enabled in the plugin configuration by the `withTrace` option