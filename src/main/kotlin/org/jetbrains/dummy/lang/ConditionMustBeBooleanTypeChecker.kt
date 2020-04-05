package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class ConditionMustBeBooleanTypeVisitor: DummyLangVisitor<Unit, MutableList<IfStatement>>() {
    private var initializedVariables = mutableMapOf<String, Expression?>()
    override fun visitElement(element: Element, data: MutableList<IfStatement>) {
        when (element) {
            is File, is FunctionDeclaration ->
                element.acceptChildren(this, data)
            is Block -> {
                val externallyInitializedVariables = initializedVariables.toMutableMap()
                element.acceptChildren(this, data)
                initializedVariables = externallyInitializedVariables
            }
            is Assignment -> {
                val variable = initializedVariables[element.variable]
                if (initializedVariables.containsKey(element.variable)) {
                    var rhs: Expression? = element.rhs
                    if (element.rhs is VariableAccess) rhs = initializedVariables[element.rhs.name]
                    if (
                        variable is BooleanConst && rhs is BooleanConst
                        || variable is IntConst && rhs is IntConst
                        || variable == null
                        || rhs is FunctionCall
                        || rhs == null
                    ) initializedVariables[element.variable] = rhs
                }
            }
            is IfStatement -> {
                val cond = element.condition
                if (cond is VariableAccess) {
                    if (
                        initializedVariables[cond.name] != null
                        && initializedVariables[cond.name] !is BooleanConst
                    ) data.add(element)
                }
                if (cond is IntConst) data.add(element)

                element.thenBlock.accept(this, data)
                element.elseBlock?.accept(this, data)
            }
            is VariableDeclaration -> {
                var initializer: Expression? = element.initializer
                if (initializer is VariableAccess) initializer = initializedVariables[initializer.name]
                initializedVariables[element.name] = initializer
            }
        }
    }
}

class ConditionMustBeBooleanTypeChecker (private val reporter: DiagnosticReporter) : AbstractChecker() {

    override fun inspect(file: File) {
        val errors = mutableListOf<IfStatement>()
        file.accept(ConditionMustBeBooleanTypeVisitor(), errors)
        errors.forEach { reportAccessBeforeInitialization(it) }
    }

    private fun reportAccessBeforeInitialization(ifStatement: IfStatement) {
//        reporter.report(ifStatement, "Condition of If statement '${ifStatement.condition}' must be boolean type")
        reporter.report(ifStatement, "Condition of If statement must be boolean type")
    }
}