/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.run

import hu.simplexion.rui.kotlin.plugin.RuiTest
import hu.simplexion.rui.kotlin.plugin.RuiTestDumpResult
import hu.simplexion.rui.kotlin.plugin.RuiTestResult
import hu.simplexion.rui.runtime.rui
import hu.simplexion.rui.runtime.testing.T1

@RuiTest
@RuiTestDumpResult
fun blockInRoot() {
    rui {
        T1(10)
        T1(11)
    }
}


@RuiTestResult
fun blockInRootResult(): String = """
[ RuiRoot                        ]  init                  |  ruiParent: null
[ RuiT1                          ]  init                  |  
[ RuiT1                          ]  init                  |  
[ RuiRoot                        ]  create                |  
[ RuiT1                          ]  create                |  p0: 10
[ RuiT1                          ]  create                |  p0: 11
[ RuiRoot                        ]  mount                 |  
[ RuiT1                          ]  mount                 |  bridge: 1
[ RuiT1                          ]  mount                 |  bridge: 1
""".trimIndent()