/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.mapper;

import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import java.util.function.Consumer;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface CasDetailsMapper extends Converter<CasDTO, UserCASDetailsEntity> {

    @Mapping(target = "folioEntities", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "casTypeEnum", source = "casType")
    @Mapping(target = "fileTypeEnum", source = "fileType")
    @Mapping(target = "investorInfoEntity", source = "investorInfo")
    @Override
    UserCASDetailsEntity convert(CasDTO casDTO);

    @Mapping(target = "schemeEntities", ignore = true)
    @Mapping(target = "userCasDetailsEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(source = "kyc", target = "kyc")
    @Mapping(source = "pan", target = "pan")
    @Mapping(source = "panKyc", target = "panKyc")
    UserFolioDetailsEntity mapUserFolioDTOToUserFolioDetailsEntity(UserFolioDTO folioDTO);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "transactionEntities", ignore = true)
    @Mapping(target = "userFolioDetailsEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    UserSchemeDetailsEntity schemeDTOToSchemeEntity(UserSchemeDTO schemeDTO);

    @Mapping(target = "userSchemeDetailsEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "transactionDate", source = "date")
    UserTransactionDetailsEntity transactionDTOToTransactionEntity(UserTransactionDTO transactionDTO);

    @AfterMapping
    default void addFolioEntityToCaseDetails(CasDTO casDTO, @MappingTarget UserCASDetailsEntity userCasDetailsEntity) {
        Consumer<UserFolioDTO> addFolioEntityConsumer =
                folioDTO -> userCasDetailsEntity.addFolioEntity(mapUserFolioDTOToUserFolioDetailsEntity(folioDTO));
        casDTO.folios().forEach(addFolioEntityConsumer);
    }

    @AfterMapping
    default void addSchemaEntityToFolioEntity(
            UserFolioDTO folioDTO, @MappingTarget UserFolioDetailsEntity userFolioDetailsEntity) {
        Consumer<UserSchemeDTO> addSchemeEntityConsumer =
                schemeDTO -> userFolioDetailsEntity.addSchemeEntity(schemeDTOToSchemeEntity(schemeDTO));
        folioDTO.schemes().forEach(addSchemeEntityConsumer);
    }

    @AfterMapping
    default void addTransactionEntityToSchemeEntity(
            UserSchemeDTO schemeDTO, @MappingTarget UserSchemeDetailsEntity userSchemeDetailsEntity) {
        Consumer<UserTransactionDTO> addTransactionEntityConsumer = transactionDTO ->
                userSchemeDetailsEntity.addTransactionEntity(transactionDTOToTransactionEntity(transactionDTO));
        schemeDTO.transactions().forEach(addTransactionEntityConsumer);
    }
}
