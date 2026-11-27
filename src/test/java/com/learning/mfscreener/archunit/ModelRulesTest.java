/* Licensed under Apache-2.0 2024. */
package com.learning.mfscreener.archunit;

import static com.learning.mfscreener.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.MODEL_PACKAGE;
import static com.learning.mfscreener.archunit.CommonRules.fieldsShouldHaveGetterRule;
import static com.learning.mfscreener.archunit.CommonRules.methodsShouldBePublicRule;
import static com.learning.mfscreener.archunit.CommonRules.springAnnotationsClassesAreNotAllowedRule;
import static com.learning.mfscreener.archunit.CommonRules.staticMethodsAreNotAllowedRule;
import static com.learning.mfscreener.archunit.CustomConditions.HAVE_EQUALS_AND_HASH_CODE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.thirdparty.com.google.common.collect.Maps;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class ModelRulesTest {

    // Classes
    @ArchTest
    static final ArchRule classesShouldOverrideEqualsAndHashCode = classes()
            .that()
            .resideInAnyPackage(MODEL_PACKAGE)
            .and()
            .areNotMemberClasses()
            .and()
            .areNotEnums()
            .and()
            .areNotInterfaces()
            .should(HAVE_EQUALS_AND_HASH_CODE)
            .because("Model classes should override equals and hashCode methods");

    @ArchTest
    static final ArchRule springAnnotationsAreNotAllowed = springAnnotationsClassesAreNotAllowedRule(MODEL_PACKAGE);

    // Fields
    @ArchTest
    static final ArchRule fieldsShouldHaveGetter =
            fieldsShouldHaveGetterRule(Maps.newHashMap(), MODEL_PACKAGE).allowEmptyShould(true);

    // Methods
    @ArchTest
    static final ArchRule methodsShouldBePublic = methodsShouldBePublicRule(MODEL_PACKAGE);

    @ArchTest
    static final ArchRule classesShouldBeRecordsOrEnumsOrInterfaceOnly = classes()
            .that()
            .resideInAPackage(MODEL_PACKAGE)
            .should(ArchCondition.ConditionByPredicate.from(
                    new DescribedPredicate<>("be records or enums or interfaces") {
                        @Override
                        public boolean test(JavaClass javaClass) {
                            return javaClass.isRecord() || javaClass.isEnum() || javaClass.isInterface();
                        }
                    }))
            .because("Resources should be records or enums or interfaces in %s".formatted(MODEL_PACKAGE));

    @ArchTest
    static final ArchRule staticMethodsAreNotAllowed = staticMethodsAreNotAllowedRule(MODEL_PACKAGE);
}
