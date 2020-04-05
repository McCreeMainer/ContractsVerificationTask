package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class NoSuchFunctionVisitor: DummyLangVisitor<Unit, MutableList<FunctionCall>>() {
    private val assignedFunctions = mutableSetOf<String>()
    override fun visitElement(element: Element, data: MutableList<FunctionCall>) {
        when (element) {
            is FunctionDeclaration, is Block, is Assignment, is IfStatement, is VariableDeclaration, is ReturnStatement ->
                element.acceptChildren(this, data)
            is File -> {
                assignedFunctions.addAll(element.functions.map { it.name })
                element.acceptChildren(this, data)
            }
            is FunctionCall ->
                if (!assignedFunctions.contains(element.function)) data.add(element)
        }
    }
}

class NoSuchFunctionChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {

    override fun inspect(file: File) {
        val errors = mutableListOf<FunctionCall>()
        file.accept(NoSuchFunctionVisitor(), errors)
        errors.forEach { reportNoSuchFunction(it) }
    }

    private fun reportNoSuchFunction(funCall: FunctionCall) {
        reporter.report(funCall, "No such function '${funCall.function}'")
    }
}