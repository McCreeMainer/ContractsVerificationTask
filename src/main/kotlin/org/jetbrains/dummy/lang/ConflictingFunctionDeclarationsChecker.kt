package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class ConflictingFunctionDeclarationsVisitor: DummyLangVisitor<Unit, MutableList<FunctionDeclaration>>() {
    private val assignedFunctions = mutableSetOf<String>()
    override fun visitElement(element: Element, data: MutableList<FunctionDeclaration>) {
        when (element) {
            is File ->
                element.acceptChildren(this, data)
            is FunctionDeclaration -> {
                if (assignedFunctions.contains(element.name)) data.add(element)
                else assignedFunctions.add(element.name)
            }
        }
    }
}

class ConflictingFunctionDeclarationsChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {
    override fun inspect(file: File) {
        val errors = mutableListOf<FunctionDeclaration>()
        file.accept(ConflictingFunctionDeclarationsVisitor(), errors)
        errors.forEach { reportConflictingFunctionDeclarations(it) }
    }

    private fun reportConflictingFunctionDeclarations(funDeclaration: FunctionDeclaration) {
        reporter.report(funDeclaration, "Function '${funDeclaration.name}' is already exist")
    }
}