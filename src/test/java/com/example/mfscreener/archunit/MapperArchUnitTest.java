/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.archunit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.mfscreener.mapper.CasDetailsMapper;
import com.example.mfscreener.mapper.MfSchemeToMfSchemeDTOMapper;
import com.example.mfscreener.mapper.NavDataToMFSchemeNavMapper;
import com.example.mfscreener.mapper.NavServiceMapper;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapperArchUnitTest {

    @Test
    void testMappers() {

        // we have some built other ArchUnit-checks that make sure we can rely on this filter
        final long mapperCount =
                new ClassFileImporter()
                        .importPackages("com.example.mfscreener.mapper").stream()
                                .filter(javaClass -> javaClass.getSimpleName().endsWith("Mapper"))
                                .count();

        final List<Object> mappers =
                List.of(
                        CasDetailsMapper.class,
                        MfSchemeToMfSchemeDTOMapper.class,
                        NavDataToMFSchemeNavMapper.class,
                        NavServiceMapper.class);

        assertThat(mappers)
                .withFailMessage("Scanned number of mappers doesn't match the provided amount")
                .hasSize(Long.valueOf(mapperCount).intValue());
    }
}
