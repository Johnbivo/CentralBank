package com.bivolaris.centralbank.mappers;


import com.bivolaris.centralbank.dtos.TransactionRequest;
import com.bivolaris.centralbank.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {


    @Mapping(target = "fromBank", ignore = true)
    @Mapping(target = "toBank", ignore = true)
    @Mapping(target = "fromAccount", ignore = true)
    @Mapping(target = "toAccount", ignore = true)
    Transaction toEntityTransaction(TransactionRequest transactionRequest);



}
