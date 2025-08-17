package com.bivolaris.centralbank.mappers;


import com.bivolaris.centralbank.dtos.EmployeeDto;
import com.bivolaris.centralbank.entities.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeDto toDto(Employee employee);
}
