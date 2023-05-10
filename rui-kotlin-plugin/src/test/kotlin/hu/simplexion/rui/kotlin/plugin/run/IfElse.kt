/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.run

import hu.simplexion.rui.kotlin.plugin.RuiTest
import hu.simplexion.rui.kotlin.plugin.RuiTestDumpResult
import hu.simplexion.rui.kotlin.plugin.RuiTestResult
import hu.simplexion.rui.runtime.Rui
import hu.simplexion.rui.runtime.rui
import hu.simplexion.rui.runtime.testing.EH1A
import hu.simplexion.rui.runtime.testing.EH1B
import hu.simplexion.rui.runtime.testing.RuiEH1A
import hu.simplexion.rui.runtime.testing.RuiTestAdapter
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

@RuiTest
@RuiTestDumpResult
fun ifElse() {
    val adapter = RuiTestAdapter()

    rui(adapter) {
        ifElseFragment()
    }

    adapter.fragments.firstIsInstance<RuiEH1A>().eventHandler(13)
    adapter.fragments.firstIsInstance<RuiEH1A>().eventHandler(14)
}

@Rui
fun ifElseFragment() {
    var i = 12
    if (i % 2 == 0) {
        EH1A(i + 10) { i++ }
    } else {
        EH1B(i + 20) { i++ }
    }
}

@RuiTestResult
fun ifElseResult(): String = """
[ RuiRoot                        ]  init                  |  ruiScope: null
[ RuiIfElseFragment              ]  init                  |  ruiScope: null
[ RuiEH1A                        ]  init                  |  p0: 22
[ RuiRoot                        ]  create                |  
[ RuiIfElseFragment              ]  create                |  
[ RuiEH1A                        ]  create                |  
[ RuiRoot                        ]  mount                 |  
[ RuiIfElseFragment              ]  mount                 |  
[ RuiEH1A                        ]  mount                 |  bridge: 2
[ RuiIfElseFragment              ]  patch                 |  ruiDirty0: 1
[ RuiEH1A                        ]  unmount               |  bridge: 2
[ RuiEH1A                        ]  dispose               |  
[ RuiEH1B                        ]  init                  |  p0: 33
[ RuiEH1B                        ]  create                |  
[ RuiEH1B                        ]  mount                 |  bridge: 2
[ RuiIfElseFragment              ]  patch                 |  ruiDirty0: 1
[ RuiEH1B                        ]  unmount               |  bridge: 2
[ RuiEH1B                        ]  dispose               |  
[ RuiEH1A                        ]  init                  |  p0: 24
[ RuiEH1A                        ]  create                |  
[ RuiEH1A                        ]  mount                 |  bridge: 2
""".trimIndent()