package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class ConflictingVariableDeclarationsVisitor: DummyLangVisitor<Unit, MutableList<VariableDeclaration>>() {
    private val initializedVariables = mutableListOf<String>()
    override fun visitElement(element: Element, data: MutableList<VariableDeclaration>) {
        when (element) {
            is File, is IfStatement ->
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
            is VariableDeclaration ->
                if (initializedVariables.contains(element.name)) data.add(element)
                else initializedVariables.add(element.name)
        }
    }
}

class ConflictingVariableDeclarationsChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {
    override fun inspect(file: File) {
        val errors = mutableListOf<VariableDeclaration>()
        file.accept(ConflictingVariableDeclarationsVisitor(), errors)
        errors.forEach { reportConflictingVariableDeclarations(it) }
    }

    private fun reportConflictingVariableDeclarations(varDeclaration: VariableDeclaration) {
        reporter.report(varDeclaration, "Variable '${varDeclaration.name}' is already exist")
    }
}