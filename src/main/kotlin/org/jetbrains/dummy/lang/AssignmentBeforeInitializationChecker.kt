package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class AssignmentBeforeInitializationVisitor: DummyLangVisitor<Unit, MutableList<Assignment>>() {
    private val declaredVariables = mutableListOf<String>()
    override fun visitElement(element: Element, data: MutableList<Assignment>) {
        when (element) {
            is File, is IfStatement ->
                element.acceptChildren(this, data)
            is FunctionDeclaration -> {
                declaredVariables.addAll(element.parameters)
                element.acceptChildren(this, data)
                declaredVariables.clear()
            }
            is Block -> {
                val externallyInitializedVariables = declaredVariables.size
                element.acceptChildren(this, data)
                while (declaredVariables.size != externallyInitializedVariables)
                    declaredVariables.removeAt(declaredVariables.lastIndex)
            }
            is Assignment -> {
                element.acceptChildren(this, data)
                if (!declaredVariables.contains(element.variable)) data.add(element)
            }
            is VariableDeclaration ->
                if (!declaredVariables.contains(element.name))
                    declaredVariables.add(element.name)
        }
    }
}

class AssignmentBeforeInitializationChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {

    override fun inspect(file: File) {
        val errors = mutableListOf<Assignment>()
        file.accept(AssignmentBeforeInitializationVisitor(), errors)
        errors.forEach { reportAssignmentBeforeInitialization(it) }
    }

    private fun reportAssignmentBeforeInitialization(assignment: Assignment) {
        reporter.report(assignment, "Variable '${assignment.variable}' is assigned before initialization")
    }
}