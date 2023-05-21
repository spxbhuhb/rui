/*
 * Copyright (C) 2020 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.simplexion.rui.kotlin.plugin.ir

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import hu.simplexion.rui.kotlin.plugin.ir.diagnostics.ErrorsRui
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorsTest {

    val sourceDir = "src/test/kotlin/hu/simplexion/rui/kotlin/plugin/ir/errors"

    @Test
    fun innerVariableShadow() = compile("InnerVariableShadow.kt", ErrorsRui.RUI_IR_STATE_VARIABLE_SHADOW)

    @Test
    fun variableInRendering() = compile("VariableInRendering.kt", ErrorsRui.RUI_IR_RENDERING_VARIABLE)

    @Test
    fun stateVariableShadow() = compile("StateVariableShadow.kt", ErrorsRui.RUI_IR_STATE_VARIABLE_SHADOW)

    @Test
    fun externalFunction() = compile("ExternalFunction.kt", ErrorsRui.RUI_IR_MISSING_FUNCTION_BODY)

    @OptIn(ExperimentalCompilerApi::class)
    fun compile(fileName: String, expectedError: ErrorsRui.RuiIrError, dumpResult: Boolean = false) {
        val result = KotlinCompilation()
            .apply {
                workingDir = File("./tmp")
                sources = listOf(
                    SourceFile.fromPath(File(sourceDir, fileName))
                )
                useIR = true
                compilerPluginRegistrars = forCompilationError()
                inheritClassPath = true
                messageOutputStream = ByteArrayOutputStream()
            }
            .compile()

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains(expectedError.toMessage()), "missing error: ${expectedError.toMessage()}")

        if (dumpResult) println(result.dump())

    }

    fun KotlinCompilation.Result.dump(): String {
        val lines = mutableListOf<String>()

        lines += "exitCode: ${this.exitCode}"

        lines += "======== Messages ========"
        lines += this.messages

        lines += "======== Generated files ========"
        this.generatedFiles.forEach {
            lines += it.toString()
        }

        return lines.joinToString("\n")
    }
}

