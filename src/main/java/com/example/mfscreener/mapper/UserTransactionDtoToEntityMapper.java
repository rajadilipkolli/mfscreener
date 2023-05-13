/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener.mapper;

import com.example.mfscreener.entities.UserTransactionDetailsEntity;
import com.example.mfscreener.models.UserTransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;

@Mapper(config = MapperSpringConfig.class)
public interface UserTransactionDtoToEntityMapper extends Converter<UserTransactionDTO, UserTransactionDetailsEntity> {

    @Mapping(target = "transactionDate", source = "date")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userSchemeDetailsEntity", ignore = true)
    @Override
    UserTransactionDetailsEntity convert(UserTransactionDTO source);
}
