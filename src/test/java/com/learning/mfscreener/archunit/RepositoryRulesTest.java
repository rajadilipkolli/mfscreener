/* Licensed under Apache-2.0 2022-2023. */
package com.learning.mfscreener.archunit;

import static com.learning.mfscreener.archunit.ArchitectureConstants.ANNOTATED_EXPLANATION;
import static com.learning.mfscreener.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.REPOSITORY_IMPL_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.REPOSITORY_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.REPOSITORY_SUFFIX;
import static com.learning.mfscreener.archunit.CommonRules.interfacesAreOnlyAllowedRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class RepositoryRulesTest {

    // Classes
    @ArchTest
    static final ArchRule classes_should_be_annotated = classes()
            .that()
            .resideInAPackage(REPOSITORY_PACKAGE)
            .and()
            .haveSimpleNameNotEndingWith("BeanDefinitions")
            .and()
            .haveSimpleNameNotStartingWith("Custom")
            .should()
            .beAnnotatedWith(Repository.class)
            .because(String.format(ANNOTATED_EXPLANATION, REPOSITORY_SUFFIX, "@Repository"));

    @ArchTest
    static final ArchRule classesShouldBeInterfaces =
            interfacesAreOnlyAllowedRule(REPOSITORY_PACKAGE, REPOSITORY_IMPL_PACKAGE);
}
