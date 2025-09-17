package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByCode(String code);

    Employee findTopByOrderByIdDesc();

    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "e.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    Page<Employee> searchEmployeeByKeyword(@Param("keyword") String keyword, Pageable pageable);



    boolean existsByPhoneNumber(String phoneNumber);

    Employee findByPhoneNumber(String phoneNumber);

    Employee findByAccount_Id(Long id);

    Employee findByAccount_Email(String email);

    boolean existsByEmail(String email);
    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}