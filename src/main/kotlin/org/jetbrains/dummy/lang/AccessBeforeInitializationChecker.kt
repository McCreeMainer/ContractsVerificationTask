package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class AccessBeforeInitializationVisitor: DummyLangVisitor<Unit, MutableList<VariableAccess>>() {
    private val initializedVariables = mutableListOf<String>()
    override fun visitElement(element: Element, data: MutableList<VariableAccess>) {
        when (element) {
            is File, is IfStatement, is ReturnStatement, is FunctionCall ->
                element.acceptChildren(this, data)
            is FunctionDeclaration -> {
                initializedVariables.addAll(element.parameters)
                element.acceptChildren(this, data)
                initializedVariables.clear()
            }
            is Block -> {
                val externallyInitializedVariables = initializedVariables.size
                element.acceptChildren(this, data)
                while (initializedVariables.size != externallyInitializedVariables)
                    initializedVariables.removeAt(initializedVariables.lastIndex)
            }
            is Assignment -> {
                element.acceptChildren(this, data)
                if (!initializedVariables.contains(element.variable)) initializedVariables.add(element.variable)
            }
            is VariableDeclaration ->
                if (element.initializer != null) {
                    element.acceptChildren(this, data)
                    initializedVariables.add(element.name)
                }
            is VariableAccess ->
                if (!initializedVariables.contains(element.name)) data.add(element)
        }
    }
}

class AccessBeforeInitializationChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {

    override fun inspect(file: File) {
        val errors = mutableListOf<VariableAccess>()
        file.accept(AccessBeforeInitializationVisitor(), errors)
        errors.forEach { reportAccessBeforeInitialization(it) }
    }

    private fun reportAccessBeforeInitialization(access: VariableAccess) {
        reporter.report(access, "Variable '${access.name}' is accessed before initialization")
    }
}