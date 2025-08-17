package com.bivolaris.centralbank.mappers;



import com.bivolaris.centralbank.dtos.UserDto;
import com.bivolaris.centralbank.entities.Employee;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {


    UserDto toUserDto(Employee employee);
}
