/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.archunit;

import static com.learning.mfscreener.archunit.ArchitectureConstants.CONTROLLER_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.MODEL_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.REPOSITORY_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.SERVICE_PACKAGE;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    private static final String CONTROLLER = "Controller";
    private static final String MODEL = "Model";
    private static final String REPOSITORY = "Repository";
    ;
    private static final String SERVICE = "Service";

    @ArchTest
    static final ArchRule layeredArchitectureRule = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer(CONTROLLER)
            .definedBy(CONTROLLER_PACKAGE)
            .layer(MODEL)
            .definedBy(MODEL_PACKAGE)
            .layer(REPOSITORY)
            .definedBy(REPOSITORY_PACKAGE)
            .layer(SERVICE)
            .definedBy(SERVICE_PACKAGE)
            .whereLayer(CONTROLLER)
            .mayNotBeAccessedByAnyLayer()
            .whereLayer(MODEL)
            .mayOnlyBeAccessedByLayers(REPOSITORY, SERVICE, CONTROLLER)
            .whereLayer(REPOSITORY)
            .mayOnlyBeAccessedByLayers(SERVICE)
            .whereLayer(SERVICE)
            .mayOnlyBeAccessedByLayers(CONTROLLER, SERVICE);
}
