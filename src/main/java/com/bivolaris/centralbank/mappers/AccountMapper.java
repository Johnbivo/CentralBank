package com.bivolaris.centralbank.mappers;


import com.bivolaris.centralbank.dtos.AccountAllDto;
import com.bivolaris.centralbank.dtos.AccountDetailsRequest;
import com.bivolaris.centralbank.entities.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "bankName", source = "bank.name")
    AccountDetailsRequest accountDetailsToDto(Account account);


    @Mapping(target = "accountId", source = "id")
    AccountAllDto accountAllToDto(Account account);
}
