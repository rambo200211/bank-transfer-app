package com.example.bankapp.repository;

import com.example.bankapp.model.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Query("""
        SELECT c FROM Customer c
        WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(c.accountNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Customer> searchCustomers(@Param("keyword") String keyword);

    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM Customer c")
    BigDecimal getTotalBalance();
}