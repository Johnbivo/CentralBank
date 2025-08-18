package com.bivolaris.centralbank.services;

import com.bivolaris.centralbank.dtos.EmployeeDto;
import com.bivolaris.centralbank.entities.Employee;
import com.bivolaris.centralbank.mappers.EmployeeMapper;
import com.bivolaris.centralbank.repositories.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public List<EmployeeDto> getAllEmployees(){
        return employeeRepository.findAllEmployees()
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    public EmployeeDto getEmployee(Long id){
       var employee = employeeRepository.findById(id).orElse(null);
       if(employee == null){
           return null;
       }
       return employeeMapper.toDto(employee);
    }

    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email).orElse(null);
    }
}
