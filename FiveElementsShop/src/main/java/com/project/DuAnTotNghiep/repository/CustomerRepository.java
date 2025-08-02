package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.dto.Statistic.TopCustomerBuy;
import com.project.DuAnTotNghiep.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCode(String code);

    Customer findTopByOrderByIdDesc();

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "c.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    Page<Customer> searchCustomerKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT TOP 5 c.code, c.name, COUNT(c.id) AS totalPurchases, SUM(b.amount) AS revenue\n" +
            "           FROM Customer c\n" +
            "           JOIN bill b ON b.customer_id = c.id\n" +
            "           JOIN bill_detail bd ON b.id = bd.bill_id\n" +
            "           GROUP BY c.id, c.name, c.code\n" +
            "           ORDER BY totalPurchases DESC", nativeQuery = true)
    List<TopCustomerBuy> findTopCustomersByPurchases();

    boolean existsByPhoneNumber(String phoneNumber);

    Customer findByPhoneNumber(String phoneNumber);

    Customer findByAccount_Id(Long id);
    Customer findByAccount_Email(String email);

    // Thêm
    boolean existsByEmail(String email); // Đã có, giữ nguyên
    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}