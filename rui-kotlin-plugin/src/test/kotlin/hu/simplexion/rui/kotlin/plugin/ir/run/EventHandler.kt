/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.run

import hu.simplexion.rui.kotlin.plugin.ir.RuiTest
import hu.simplexion.rui.kotlin.plugin.ir.RuiTestDumpResult
import hu.simplexion.rui.kotlin.plugin.ir.RuiTestResult
import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.rui
import hu.simplexion.rui.runtime.testing.EH1A
import hu.simplexion.rui.runtime.testing.RuiEH1A
import hu.simplexion.rui.runtime.testing.RuiTestAdapter
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

@RuiTest
@RuiTestDumpResult
fun eventHandlerTest() {
    val adapter = RuiTestAdapter()

    rui(adapter) {
        eventHandlerFragment()
    }

    adapter.fragments.firstIsInstance<RuiEH1A>().eventHandler(12)
}

@Rui
fun eventHandlerFragment() {
    var i = 12
    EH1A(i + 1) { i++ }
}

@RuiTestResult
fun eventHandlerTestResult(): String = """
[ RuiRoot                        ]  init                  |  ruiScope: null
[ RuiEventHandlerFragment        ]  init                  |  ruiScope: null
[ RuiEH1A                        ]  init                  |  p0: 13
[ RuiRoot                        ]  create                |  
[ RuiEventHandlerFragment        ]  create                |  
[ RuiEH1A                        ]  create                |  
[ RuiRoot                        ]  mount                 |  
[ RuiEventHandlerFragment        ]  mount                 |  
[ RuiEH1A                        ]  mount                 |  bridge: 1
[ RuiEventHandlerFragment        ]  patch                 |  scopeMask: 1 ruiDirty0: 1
[ RuiEH1A                        ]  invalidate            |  mask: 1 ruiDirty0: 0
[ RuiEH1A                        ]  patch                 |  ruiDirty0: 1 p0: 14
""".trimIndent()