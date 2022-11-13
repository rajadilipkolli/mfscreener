/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.entities.CASDetailsEntity;
import com.example.mfscreener.entities.FolioEntity;
import com.example.mfscreener.entities.SchemeEntity;
import com.example.mfscreener.entities.TransactionEntity;
import com.example.mfscreener.models.CasDTO;
import com.example.mfscreener.models.FolioDTO;
import com.example.mfscreener.models.SchemeDTO;
import com.example.mfscreener.models.TransactionDTO;
import java.util.function.Consumer;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface CasDetailsMapper extends Converter<CasDTO, CASDetailsEntity> {

    @Mapping(target = "folioEntities", ignore = true)
    @Mapping(target = "investorInfoEntity", source = "investorInfo")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Override
    CASDetailsEntity convert(CasDTO casDTO);

    @Mapping(target = "schemeEntities", ignore = true)
    @Mapping(target = "casDetailsEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(source = "kyc", target = "kyc")
    @Mapping(source = "pan", target = "pan")
    @Mapping(source = "panKyc", target = "panKyc")
    FolioEntity folioDTOToFolioEntity(FolioDTO folioDTO);

    @Mapping(target = "transactionEntities", ignore = true)
    @Mapping(target = "folioEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    SchemeEntity schemeDTOToSchemeEntity(SchemeDTO schemeDTO);

    @Mapping(target = "schemeEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "transactionDate", source = "date")
    TransactionEntity transactionDTOToTransactionEntity(TransactionDTO transactionDTO);

    @AfterMapping
    default void addFolioEntityToCaseDetails(
            CasDTO casDTO, @MappingTarget CASDetailsEntity casDetailsEntity) {
        Consumer<FolioDTO> addFolioEntityConsumer =
                folioDTO -> casDetailsEntity.addFolioEntity(folioDTOToFolioEntity(folioDTO));
        casDTO.folios().forEach(addFolioEntityConsumer);
    }

    @AfterMapping
    default void addSchemaEntityToFolioEntity(
            FolioDTO folioDTO, @MappingTarget FolioEntity folioEntity) {
        Consumer<SchemeDTO> addSchemeEntityConsumer =
                schemeDTO -> folioEntity.addSchemeEntity(schemeDTOToSchemeEntity(schemeDTO));
        folioDTO.schemes().forEach(addSchemeEntityConsumer);
    }

    @AfterMapping
    default void addTransactionEntityToSchemeEntity(
            SchemeDTO schemeDTO, @MappingTarget SchemeEntity schemeEntity) {
        Consumer<TransactionDTO> addTransactionEntityConsumer =
                transactionDTO ->
                        schemeEntity.addTransactionEntity(
                                transactionDTOToTransactionEntity(transactionDTO));
        schemeDTO.transactions().forEach(addTransactionEntityConsumer);
    }
}
