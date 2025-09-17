package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.dto.Employee.EmployeeDto;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    Page<EmployeeDto> getAllEmployees(Pageable pageable);

    EmployeeDto createEmployeeAdmin(EmployeeDto employeeDto);

    Page<EmployeeDto> searchEmployeeAdmin(String keyword, Pageable pageable);

    EmployeeDto getEmployeeById(Long id);

    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);

    void deleteEmployee(Long id);

    Employee getLastEmployee();

    Account blockAccount(Long id); // Khóa tài khoản của nhân viên

    Account openAccount(Long id); // Mở khóa tài khoản của nhân viên

    Account getAccountByEmployeeId(Long employeeId); // Lấy thông tin tài khoản theo ID nhân viên
}