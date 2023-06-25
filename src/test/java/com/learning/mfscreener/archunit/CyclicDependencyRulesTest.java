/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.archunit;

import static com.learning.mfscreener.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class CyclicDependencyRulesTest {

    @ArchTest
    public static final ArchRule noCyclesDependencies =
            slices().matching("..(*)..").should().beFreeOfCycles();
}
