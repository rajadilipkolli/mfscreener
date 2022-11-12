/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import org.mapstruct.MapperConfig;
import org.mapstruct.extensions.spring.SpringMapperConfig;

@MapperConfig(componentModel = "spring", uses = ConversionServiceAdapter.class)
@SpringMapperConfig(conversionServiceAdapterPackage = "com.example.mfscreener.adapter")
public interface MapperSpringConfig {}
