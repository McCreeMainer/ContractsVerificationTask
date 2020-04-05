package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class VariableTypeMismatchVisitor: DummyLangVisitor<Unit, MutableList<Assignment>>() {
    private var initializedVariables = mutableMapOf<String, Expression?>()
    override fun visitElement(element: Element, data: MutableList<Assignment>) {
        when (element) {
            is File, is FunctionDeclaration, is IfStatement ->
                element.acceptChildren(this, data)
            is Block -> {
                val externallyInitializedVariables = initializedVariables.toMutableMap()
                element.acceptChildren(this, data)
                initializedVariables = externallyInitializedVariables
            }
            is Assignment -> {
                val variable = initializedVariables[element.variable]
                if (variable != null) {
                    var rhs: Expression? = element.rhs
                    if (element.rhs is VariableAccess)
                        rhs = initializedVariables[element.rhs.name]
                    if (
                        variable is BooleanConst && rhs is BooleanConst
                        || variable is IntConst && rhs is IntConst
                        || rhs is FunctionCall
                        || rhs == null
                    ) initializedVariables[element.variable] = rhs
                    else data.add(element)
                }
            }
            is VariableDeclaration -> {
                var initializer: Expression? = element.initializer
                if (initializer is VariableAccess) initializer = initializedVariables[initializer.name]
                initializedVariables[element.name] = initializer
            }
        }
    }
}

class VariableTypeMismatchChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {

    override fun inspect(file: File) {
        val errors = mutableListOf<Assignment>()
        file.accept(VariableTypeMismatchVisitor(), errors)
        errors.forEach { reportVariableTypeMismatch(it) }
    }

    private fun reportVariableTypeMismatch(assignment: Assignment) {
        reporter.report(assignment, "Variable '${assignment.variable}' has a different type")
    }
}