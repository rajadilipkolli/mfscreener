/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.entities.CASDetailsEntity;
import com.example.mfscreener.entities.FolioEntity;
import com.example.mfscreener.entities.SchemeEntity;
import com.example.mfscreener.models.CasDTO;
import com.example.mfscreener.models.FolioDTO;
import com.example.mfscreener.models.SchemeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface CasDetailsMapper extends Converter<CasDTO, CASDetailsEntity> {

    @Mapping(target = "investorInfoEntity", source = "investorInfo")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "folioEntities", source = "folios")
    @Override
    CASDetailsEntity convert(CasDTO casDTO);

    @Mapping(target = "casDetailsEntity", ignore = true)
    @Mapping(target = "schemeEntities", source = "schemes")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(source = "KYC", target = "kyc")
    @Mapping(source = "PAN", target = "pan")
    @Mapping(source = "PANKYC", target = "panKyc")
    FolioEntity folioDTOToFolioEntity(FolioDTO folioDTO);

    @Mapping(target = "folioEntity", ignore = true)
    @Mapping(target = "transactionEntities", source = "transactions")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    SchemeEntity schemeDTOToSchemeEntity(SchemeDTO schemeDTO);
}
