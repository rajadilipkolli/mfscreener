/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.archunit;

import static com.learning.mfscreener.archunit.ArchitectureConstants.ANNOTATED_EXPLANATION;
import static com.learning.mfscreener.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.ENTITIES_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.ENTITY_SUFFIX;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
public class EntityRulesTest {

    @ArchTest
    static final ArchRule classes_should_be_annotated = classes()
            .that()
            .resideInAPackage(ENTITIES_PACKAGE)
            .and()
            .haveSimpleNameNotEndingWith("Enum")
            .and()
            .doNotHaveSimpleName("AuditableEntity")
            .should()
            .beAnnotatedWith(Entity.class)
            .andShould()
            .beAnnotatedWith(Table.class)
            .because(ANNOTATED_EXPLANATION.formatted(ENTITY_SUFFIX, "@Entity"));

    @ArchTest
    static final ArchRule CLASSES_SHOULD_END_WITH_NAME_RULE =
            classes().that().areAnnotatedWith(Entity.class).should().haveSimpleNameEndingWith("Entity");
}
