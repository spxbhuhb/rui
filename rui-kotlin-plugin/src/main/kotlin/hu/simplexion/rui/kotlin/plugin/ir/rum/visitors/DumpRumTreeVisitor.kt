/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.rui.kotlin.plugin.ir.rum.visitors

import hu.simplexion.rui.kotlin.plugin.ir.rum.*
import org.jetbrains.kotlin.utils.Printer

class DumpRumTreeVisitor(
    out: Appendable
) : RumElementVisitorVoid<Unit> {

    private val printer = Printer(out, "  ")

    override fun visitElement(element: RumElement) {
        element.acceptChildren(this, null)
    }

    override fun visitEntryPoint(rumEntryPoint: RumEntryPoint) {
        indented {
            println { "ENTRY_POINT class:${rumEntryPoint.rumClass.name}" }
            super.visitEntryPoint(rumEntryPoint)
        }
    }

    override fun visitClass(rumClass: RumClass) {
        indented {
            with(rumClass) {
                println { "CLASS name:$name boundary:(${boundary.startOffset},${boundary.statementIndex})" }
            }
            super.visitClass(rumClass)
        }
    }

    override fun visitExternalStateVariable(stateVariable: RumExternalStateVariable) {
        indented {
            with(stateVariable) {
                println { "EXTERNAL_STATE_VARIABLE index:$index name:$name" }
            }
            super.visitStateVariable(stateVariable)
        }
    }

    override fun visitInternalStateVariable(stateVariable: RumInternalStateVariable) {
        indented {
            with(stateVariable) {
                println { "INTERNAL_STATE_VARIABLE index:$index name:$name" }
            }
            super.visitStateVariable(stateVariable)
        }
    }

    override fun visitDirtyMask(dirtyMask: RumDirtyMask) {
        indented {
            with(dirtyMask) {
                println { "DIRTY_MASK index:$index name:$name" }
            }
            super.visitDirtyMask(dirtyMask)
        }
    }

    override fun visitBlock(statement: RumBlock) {
        indented {
            with(statement) {
                println { "BLOCK index:$index name:$name startOffset: ${statement.irBlock.startOffset}" }
            }
            super.visitBlock(statement)
        }
    }

    override fun visitCall(statement: RumCall) {
        indented {
            with(statement) {
                println { "CALL index:$index name:$name startOffset: ${statement.irCall.startOffset} type:<$target>" }
            }
            super.visitCall(statement)
        }
    }

    override fun visitHigherOrderCall(statement: RumHigherOrderCall) {
        indented {
            with(statement) {
                println { "HIGHER_ORDER_CALL index:$index name:$name type:<$target>" }
            }
            super.visitHigherOrderCall(statement)
        }
    }

    override fun visitWhen(statement: RumWhen) {
        indented {
            with(statement) {
                println { "WHEN index:$index name:$name" }
            }
            super.visitWhen(statement)
        }
    }

    override fun visitForLoop(statement: RumForLoop) {
        indented {
            with(statement) {
                println { "FOR_LOOP index:$index name:$name" }
            }
            super.visitForLoop(statement)
        }
    }

    override fun visitBranch(branch: RumBranch) {
        indented {
            with(branch) {
                println { "BRANCH index:$index name:$name" }
            }
            super.visitBranch(branch)
        }
    }

    override fun visitExpression(expression: RumExpression) {
        indented {
            with(expression) {
                println { "$origin ${dependencies.withLabel("dependencies")}" }
            }
            super.visitExpression(expression)
        }
    }

    override fun visitValueArgument(valueArgument: RumValueArgument) {
        indented {
            with(valueArgument) {
                println { "$origin $index ${dependencies.withLabel("dependencies")}" }
            }
        }
    }

    override fun visitHigherOrderArgument(higherOrderArgument: RumHigherOrderArgument) {
        indented {
            with(higherOrderArgument) {
                println { "$origin $index ${dependencies.withLabel("dependencies")}" }
            }
        }
    }

    override fun visitDeclaration(declaration: RumDeclaration) {
        indented {
            with(declaration) {
                println { "$origin ${dependencies.withLabel("dependencies")}" }
            }
            super.visitDeclaration(declaration)
        }
    }


    private inline fun println(body: () -> String) {
        printer.println(body())
    }

    private inline fun indented(body: () -> Unit) {
        printer.pushIndent()
        body()
        printer.popIndent()
    }

    fun RumDependencies.withLabel(label: String) =
        "$label:[${this.joinToString(", ") { it.toString() }}]"

}