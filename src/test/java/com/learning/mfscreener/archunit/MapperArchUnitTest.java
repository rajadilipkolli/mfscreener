/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.archunit;

import static com.learning.mfscreener.archunit.ArchitectureConstants.ANNOTATED_EXPLANATION;
import static com.learning.mfscreener.archunit.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.MAPPER_PACKAGE;
import static com.learning.mfscreener.archunit.ArchitectureConstants.MAPPER_SUFFIX;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

import com.learning.mfscreener.mapper.CasDetailsMapper;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.mapper.MfSchemeEntityToDtoMapper;
import com.learning.mfscreener.mapper.NavDataToMFSchemeNavMapper;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class MapperArchUnitTest {

    @ArchTest
    static final ArchRule classes_should_be_annotated = classes()
            .that()
            .resideInAPackage(MAPPER_PACKAGE)
            .and()
            .haveSimpleNameEndingWith("Helper")
            .should()
            .beAnnotatedWith(Component.class)
            .because(ANNOTATED_EXPLANATION.formatted(MAPPER_SUFFIX, "@Component"));

    @ArchTest
    static final ArchRule classesShouldBeInterfaceOnly = classes()
            .that()
            .resideInAPackage(MAPPER_PACKAGE)
            .and()
            .haveSimpleNameNotEndingWith("Impl")
            .and()
            .haveSimpleNameNotEndingWith("Helper")
            .should()
            .beInterfaces()
            .because("Resources should be interfaces in %s".formatted(MAPPER_PACKAGE));

    @Test
    void mappers() {

        // we have some built other ArchUnit-checks that make sure we can rely on this filter
        final long mapperCount = new ClassFileImporter()
                .importPackages("com.learning.mfscreener.mapper").stream()
                        .filter(javaClass -> javaClass.getSimpleName().endsWith("Mapper"))
                        .count();

        final List<Object> mappers = List.of(
                MfSchemeDtoToEntityMapper.class,
                MfSchemeEntityToDtoMapper.class,
                NavDataToMFSchemeNavMapper.class,
                CasDetailsMapper.class);

        assertThat(mappers)
                .withFailMessage("Scanned number of mappers doesn't match the provided amount")
                .hasSize(Long.valueOf(mapperCount).intValue());
    }
}
