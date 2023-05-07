# Troubleshooting

## Compilation Problems

To troubleshoot plugin related problems you can enable logging in `build.gradle.kts`:

Dump points are points during the compilation process where the plugin is able to dump
some information.

`pluginLogDir` is the directory where the dumps are written. The plugin creates a new
log file for each module fragment compilation.

**NOTE** These dumps are large and complex. Best practice is to keep them off, unless
you are developing the plugin itself.

```kotlin
rui {
    dumpPoints.set(listOf("before", "after", "rui-tree", "kotlin-like"))
    pluginLogDir.set("$projectDir/tmp/log")
}
```

## Runtime Problems

Try to compile your code with trace enabled. When trace is enabled, components calls the `trace` function of the
adapter when something happens. `RuiDOMAdapter` prints out the trace, `RuiTestAdapter` collects it into a list.
You can easily add your own adapter to process the trace whichever way you like.

**NOTE** trace adds quite a lot of code to the plugin, thus the size of the resulting code will grow quite a lot.
It is good practice to have trace switched off.

```kotlin
rui {
    trace.set(true)
}
```