package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class InvalidCountOfArgumentsVisitor: DummyLangVisitor<Unit, MutableList<FunctionCall>>() {
    private val assignedFunctions = mutableMapOf<String, Int>()
    override fun visitElement(element: Element, data: MutableList<FunctionCall>) {
        when (element) {
            is FunctionDeclaration, is Block, is Assignment, is IfStatement, is VariableDeclaration, is ReturnStatement ->
                element.acceptChildren(this, data)
            is File -> {
                assignedFunctions.putAll(element.functions.map { it.name to it.parameters.size })
                element.acceptChildren(this, data)
            }
            is FunctionCall ->
                if (assignedFunctions[element.function] ?: 0 != element.arguments.size) data.add(element)
        }
    }
}

class InvalidCountOfArgumentsChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {

    override fun inspect(file: File) {
        val errors = mutableListOf<FunctionCall>()
        file.accept(InvalidCountOfArgumentsVisitor(), errors)
        errors.forEach { reportInvalidCountOfArguments(it) }
    }

    private fun reportInvalidCountOfArguments(functionCall: FunctionCall) {
        reporter.report(functionCall, "Function call '${functionCall.function}' has invalid count of arguments")
    }
}