package hu.simplexion.rui.kotlin.plugin.ir

import hu.simplexion.rui.kotlin.plugin.RuiCompilerPluginRegistrar

/**
 * For plugin debugging when the developer runs unit tests manually. Should not be committed.
 */
fun forPluginDevelopment(pluginLogDir: String? = null) = // for manual running of unit tests
    listOf(
        RuiCompilerPluginRegistrar(
            RuiDumpPoint.values().toList(),
            RuiRootNameStrategy.NoPostfix,
            trace = true,
            exportState = true,
            importState = true,
            printDumps = true,
            pluginLogDir
        )
    )

/**
 * For unit tests that should produce a comparable trace for validation.
 */
fun forValidatedResult(pluginLogDir: String? = null) = // development settings
    listOf(
        RuiCompilerPluginRegistrar(
            dumpPoints = if (pluginLogDir == null) emptyList() else RuiDumpPoint.values().toList(),
            RuiRootNameStrategy.NoPostfix,
            trace = true,
            pluginLogDir = pluginLogDir
        )
    )

fun forProduction(pluginLogDir: String? = null) =
    listOf(
        RuiCompilerPluginRegistrar(
            dumpPoints = if (pluginLogDir == null) emptyList() else RuiDumpPoint.values().toList(),
            pluginLogDir = pluginLogDir
        )
    )

/**
 * For unit tests that are supposed to generate an error.
 */
fun forCompilationError(pluginLogDir: String? = null) =
    listOf(
        RuiCompilerPluginRegistrar(
            pluginLogDir = pluginLogDir
        )
    )
