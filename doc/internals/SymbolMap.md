## Symbol Maps

The plugin uses symbol maps to get symbols necessary for building the IR. For example, when `patch` has to find
the symbol of `patch` of another component.

[RuiSymbolMap.kt](../../rui-kotlin-plugin/src/main/kotlin/hu/simplexion/rui/kotlin/plugin/ir/RuiSymbolMap.kt) contains
utilities to get an instance of `RuiClassSymbols` for any given class.

`RuiClassSymbols` then can be used to get the function / property symbols of any Rui class.

There are a few important details to note.

There may be classes which are loaded from dependencies, the Kotlin compiler loads them, `RuiSymbolMap` finds the
symbols in the data structure provided by the compiler.

Classes compiled in the current module fragment have their symbols ready **after** the RUM to AIR phase. Therefore,
symbol maps should be used only in the AIR to IR phase.

To get a symbol map, use:

- `FqName.symbolMap` from any ClassBoundIrBuilder
- `RumRenderingStatement.symbolMap(irBuilder : ClassBoundIrBuilder)` from anywhere
