package org.jetbrains.dummy.lang

import org.junit.Test

class DummyLanguageTestGenerated : AbstractDummyLanguageTest() {
    @Test
    fun testBad() {
        doTest("testData/bad.dummy")
    }

    @Test
    fun testGood() {
        doTest("testData/good.dummy")
    }

    @Test
    fun testAccessBeforeInitialization() {
        doTest("testData/AccessBeforeInitializationDiagnostic.dummy")
    }

    @Test
    fun testAssignmentBeforeInitialization() {
        doTest("testData/AssignmentBeforeInitializationDiagnostic.dummy")
    }

    @Test
    fun testConflictingDeclarations() {
        doTest("testData/ConflictingDeclarationsDiagnostic.dummy")
    }

    @Test
    fun testNoSuchFunction() {
        doTest("testData/NoSuchFunctionDiagnostic.dummy")
    }

    @Test
    fun testInvalidCountOfArguments() {
        doTest("testData/InvalidCountOfArgumentsDiagnostic.dummy")
    }

    @Test
    fun testVariableTypeMismatch() {
        doTest("testData/VariableTypeMismatchDiagnostic.dummy")
    }

    @Test
    fun testConditionMustBeBooleanType() {
        doTest("testData/ConditionMustBeBooleanTypeDiagnostic.dummy")
    }

    @Test
    fun testConflictingFunctionDeclarations() {
        doTest("testData/ConflictingFunctionDeclarationsDiagnostic.dummy")
    }}
