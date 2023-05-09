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

package hu.simplexion.rui.kotlin.plugin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class SuccessesTest {

    val sourceDir = "src/test/kotlin/hu/simplexion/rui/kotlin/plugin/successes"

    @Test
    fun sequence() = compile("Sequence.kt")

    @Test
    fun innerVariableShadow() = compile("InnerVariableShadow.kt")

    @Test
    fun variables() = compile("Variables.kt")

    @Test
    fun manyVariables() = compile("ManyVariables.kt")

    @Test
    fun basic() = compile("Basic.kt")

    fun compile(fileName: String, manual: Boolean = false) {
        compile(fileName, production = false, manual)
        compile(fileName, production = true, manual)
    }

    @OptIn(ExperimentalCompilerApi::class)
    fun compile(fileName: String, production: Boolean, manual: Boolean = false) {

        val registrar = when {
            manual -> forPluginDevelopment()
            production -> forProduction()
            else -> forValidatedResult()
        }

        val result = KotlinCompilation()
            .apply {
                workingDir = File("./tmp")
                sources = listOf(
                    SourceFile.fromPath(File(sourceDir, fileName))
                )
                useIR = true
                compilerPluginRegistrars = registrar
                inheritClassPath = true
            }
            .compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        if (manual) println(result.dump())
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

