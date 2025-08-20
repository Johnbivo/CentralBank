package com.bivolaris.centralbank.mappers;


import com.bivolaris.centralbank.dtos.BankDto;
import com.bivolaris.centralbank.entities.Bank;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankMapper {

    @Mapping(target = "bankId", source = "id")
    @Mapping(target = "bankName", source = "name")
    @Mapping(target = "bankApiEndpoint", source = "base_api_endpoint")
    @Mapping(target = "createdAt", source = "created_at")
    @Mapping(target = "updatedAt", source = "updated_at")
    @Mapping(target = "bankStatus", source = "status")
    BankDto toBankDto(Bank bank);
}
