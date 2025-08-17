package com.bivolaris.centralbank.controllers;


import com.bivolaris.centralbank.dtos.EmployeeDto;
import com.bivolaris.centralbank.services.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/employees")
public class EmployeeController {


    private final EmployeeService employeeService;


    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees(){
       List<EmployeeDto> employees = employeeService.getAllEmployees();
       if(employees.isEmpty()){
           return ResponseEntity.notFound().build();
       }
       return ResponseEntity.ok(employees);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id){
        var employee = employeeService.getEmployee(id);
        if(employee == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }



}
