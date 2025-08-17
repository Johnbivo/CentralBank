package com.bivolaris.centralbank.dtos;


import lombok.Data;

@Data
public class EmployeeDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profession;
    private String phoneNumber;
    private String address;
    private String city;

}
