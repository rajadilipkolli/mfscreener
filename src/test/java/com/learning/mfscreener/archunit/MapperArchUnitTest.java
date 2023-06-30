/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.archunit;

import static org.assertj.core.api.Assertions.assertThat;

import com.learning.mfscreener.mapper.CasDetailsMapper;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.mapper.MfSchemeEntityToDtoMapper;
import com.learning.mfscreener.mapper.NavDataToMFSchemeNavMapper;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapperArchUnitTest {

    @Test
    void testMappers() {

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
