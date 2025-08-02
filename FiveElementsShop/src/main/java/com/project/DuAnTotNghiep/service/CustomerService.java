package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CustomerService {

    Page<CustomerDto> getAllCustomers(Pageable pageable);

    CustomerDto createCustomerAdmin(CustomerDto customerDto);

    Page<CustomerDto> searchCustomerAdmin(String keyword, Pageable pageable);

    CustomerDto getCustomerById(Long id);

    CustomerDto updateCustomer(Long id, CustomerDto customerDto);

    void deleteCustomer(Long id);

    Customer getLastCustomer();

    Account blockAccount(Long id); // Khóa tài khoản của khách hàng

    Account openAccount(Long id); // Mở khóa tài khoản của khách hàng

    Account getAccountByCustomerId(Long customerId); // Lấy thông tin tài khoản theo ID khách hàng


}