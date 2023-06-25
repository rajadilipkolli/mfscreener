/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.mapper;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceAdapterPackage = "com.learning.mfscreener.adapter")
public interface MapperSpringConfig {}
