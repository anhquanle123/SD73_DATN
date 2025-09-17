package com.project.DuAnTotNghiep.dto.Employee;

import com.project.DuAnTotNghiep.entity.enumClass.RoleName;
import lombok.Data;

import java.util.Date;

@Data
public class EmployeeDto {
    private Long id;
    private String code;
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean gender;
    private String tinhThanh;
//    private RoleName role;
}