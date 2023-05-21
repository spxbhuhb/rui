/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir

import hu.simplexion.rui.kotlin.plugin.ir.air.AirClass
import hu.simplexion.rui.kotlin.plugin.ir.plugin.RuiOptions
import hu.simplexion.rui.kotlin.plugin.ir.rum.RumEntryPoint
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.fileEntry
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.FqName
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.appendText
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

@OptIn(FirIncompatiblePluginAPI::class)
class RuiPluginContext(
    val irContext: IrPluginContext,
    options: RuiOptions,
    val diagnosticReporter: IrMessageLogger,
    val moduleFragment: IrModuleFragment
) {
    val annotations = options.annotations
    val dumpPoints = options.dumpPoints
    val rootNameStrategy = options.rootNameStrategy
    val withTrace = options.withTrace
    val exportState = options.exportState
    val importState = options.importState
    val printDumps = options.printDumps

    val pluginLogDir: Path? = options.pluginLogDir?.let { Paths.get(options.pluginLogDir).also { it.createDirectories() } }
    val pluginLogTimestamp: String = DateTimeFormatter.ofPattern("yyyyMMdd'-'HHmmss").format(LocalDateTime.now())
    val pluginLogFile: Path? = pluginLogDir?.resolve("rui-log-$pluginLogTimestamp.txt").also { it?.createFile() }

    var compilationError = false

    val rumEntryPoints = mutableListOf<RumEntryPoint>()

    var airClasses = mutableMapOf<FqName, AirClass>()

    val ruiFragmentClass = classSymbol(RUI_FQN_FRAGMENT_CLASS)
    val ruiFragmentType = ruiFragmentClass.defaultType

    val ruiGeneratedFragmentClass = classSymbol(RUI_FQN_GENERATED_FRAGMENT_CLASS)

    val ruiAdapterClass = classSymbol(RUI_FQN_ADAPTER_CLASS)
    val ruiAdapterType = ruiAdapterClass.defaultType
    val ruiAdapterTrace = ruiAdapterClass.functionByName(RUI_ADAPTER_TRACE)

    val ruiBridgeClass = classSymbol(RUI_FQN_BRIDGE_CLASS)
    val ruiBridgeType = ruiBridgeClass.defaultType

    val ruiAdapter = property(RUI_ADAPTER)
    val ruiScope = property(RUI_SCOPE)
    val ruiExternalPatch = property(RUI_EXTERNAL_PATCH)
    val ruiFragment = property(RUI_FRAGMENT)

    val ruiCreate = function(RUI_CREATE)
    val ruiMount = function(RUI_MOUNT)
    val ruiPatch = function(RUI_PATCH)
    val ruiDispose = function(RUI_DISPOSE)
    val ruiUnmount = function(RUI_UNMOUNT)

    val ruiSymbolMap = RuiSymbolMap(this)

    val implicit0SymbolMap = ruiSymbolMap.getSymbolMap(RUI_FQN_IMPLICIT0_CLASS)

    private fun classSymbol(name: FqName): IrClassSymbol =
        requireNotNull(irContext.referenceClass(name)) { "missing class: ${name.asString()}" }

    private fun property(name: String) =
        ruiGeneratedFragmentClass.owner.properties.filter { it.name.asString() == name }.map { it.symbol }.toList()

    private fun function(name: String) =
        listOf(ruiGeneratedFragmentClass.functions.single { it.owner.name.asString() == name })

    fun output(title: String, content: String, declaration: IrDeclaration? = null) {

        val longTitle = "\n\n====  $title  ================================================================\n"

        pluginLogFile?.appendText("$longTitle\n\n$content")

        if (printDumps) {
            println(longTitle)
            println(content)
        } else {
            diagnosticReporter.report(
                IrMessageLogger.Severity.INFO,
                title,
                IrMessageLogger.Location(
                    declaration?.fileEntry?.name ?: moduleFragment.name.asString(),
                    declaration?.fileEntry?.getLineNumber(declaration.startOffset) ?: 1,
                    declaration?.fileEntry?.getColumnNumber(declaration.startOffset) ?: 1
                )
            )
        }
    }
}

