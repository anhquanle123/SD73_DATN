package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByEmail(String email);

    @Query(value = "SELECT CONCAT('T', MONTH(a.create_date)) AS month, COUNT(a.id) AS count FROM Account a " +
            "WHERE a.create_date BETWEEN :startDate AND :endDate " +
            "GROUP BY MONTH(a.create_date)", nativeQuery = true)
    List<Object[]> getMonthlyAccountStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    Account findByCustomer_PhoneNumber(String phoneNumber);

    Account findTopByOrderByIdDesc();

    @Query("SELECT a FROM Account a WHERE a.employee.id = :employeeId")
    Optional<Account> findByEmployeeId(@Param("employeeId") Long employeeId);

    void deleteByEmail(String email);

    Optional<Account> findByCustomerId(Long customerId);

    boolean existsByEmail(String email);
}