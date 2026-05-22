/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.archunit;

import static com.learning.mfscreener.archunit.ArchitectureConstants.ANNOTATED_EXPLANATION;
import static com.learning.mfscreener.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.SERVICE_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.SERVICE_SUFFIX;
import static com.learning.mfscreener.archunit.CommonRules.beanMethodsAreNotAllowedRule;
import static com.learning.mfscreener.archunit.CommonRules.componentAnnotationIsNotAllowedRule;
import static com.learning.mfscreener.archunit.CommonRules.fieldsShouldNotBePublic;
import static com.learning.mfscreener.archunit.CommonRules.privateMethodsAreNotAllowedRule;
import static com.learning.mfscreener.archunit.CommonRules.publicConstructorsRule;
import static com.learning.mfscreener.archunit.CommonRules.staticMethodsAreNotAllowedRule;
import static com.learning.mfscreener.archunit.CustomConditions.beAnnotatedWithTransactionalReadOnlyTrue;
import static com.learning.mfscreener.archunit.CustomConditions.notBeAnnotatedWithTransactionalReadOnlyTrue;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class ServiceRulesTest {

    // Classes
    @ArchTest
    static final ArchRule component_annotation_is_not_allowed = componentAnnotationIsNotAllowedRule(SERVICE_PACKAGE);

    @ArchTest
    static final ArchRule classes_should_be_annotated = classes()
            .that()
            .resideInAPackage(SERVICE_PACKAGE)
            .and()
            .areTopLevelClasses()
            .should()
            .beAnnotatedWith(Service.class)
            .andShould(beAnnotatedWithTransactionalReadOnlyTrue)
            .because(ANNOTATED_EXPLANATION.formatted(SERVICE_SUFFIX, "@Service and @Transactional(readOnly = true)"));

    @ArchTest
    static final ArchRule methods_should_not_be_annotated_transactionReadOnly = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(SERVICE_PACKAGE)
            .should(notBeAnnotatedWithTransactionalReadOnlyTrue)
            .because("Methods in service classes should not be annotated with @Transactional(readOnly = true)");

    // Fields
    @ArchTest
    static final ArchRule fields_should_not_be_public = fieldsShouldNotBePublic(SERVICE_PACKAGE);

    // Constructors
    @ArchTest
    static final ArchRule constructors_should_not_be_private = publicConstructorsRule(SERVICE_PACKAGE);

    // Methods
    @ArchTest
    static final ArchRule bean_methods_are_not_allowed = beanMethodsAreNotAllowedRule(SERVICE_PACKAGE);

    @ArchTest
    static final ArchRule private_methods_are_not_allowed = privateMethodsAreNotAllowedRule(SERVICE_PACKAGE);

    @ArchTest
    static final ArchRule static_methods_are_not_allowed = staticMethodsAreNotAllowedRule(SERVICE_PACKAGE);
}
