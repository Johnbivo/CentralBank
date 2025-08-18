package com.bivolaris.centralbank.mappers;

import com.bivolaris.centralbank.dtos.FraudCaseDto;
import com.bivolaris.centralbank.entities.Fraudcase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FraudCaseMapper {

    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "amount", source = "transaction.amount")
    @Mapping(target = "currency", source = "transaction.currency")
    @Mapping(target = "fromAccountNumber", source = "transaction.fromAccount.accountNumber")
    @Mapping(target = "toAccountNumber", source = "transaction.toAccount.accountNumber")
    @Mapping(target = "fromBankName", source = "transaction.fromBank.name")
    @Mapping(target = "toBankName", source = "transaction.toBank.name")
    @Mapping(target = "reviewedByEmployeeName", source = "reviewedBy", qualifiedByName = "mapEmployeeFullName")
    FraudCaseDto toDto(Fraudcase fraudcase);

    @Named("mapEmployeeFullName")
    default String mapEmployeeFullName(com.bivolaris.centralbank.entities.Employee employee) {
        if (employee == null) {
            return null;
        }
        return employee.getFirstName() + " " + employee.getLastName();
    }
}
