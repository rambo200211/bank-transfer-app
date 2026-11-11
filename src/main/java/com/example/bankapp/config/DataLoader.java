package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        if (customerRepository.count() == 0) {

            List<Customer> customers = List.of(

                    Customer.builder()
                            .accountNumber("100001")
                            .fullName("Kamal")
                            .email("kamal@example.com")
                            .balance(new BigDecimal("50000.00"))
                            .build(),

                    Customer.builder()
                            .accountNumber("100002")
                            .fullName("Nimali")
                            .email("nimali@example.com")
                            .balance(new BigDecimal("25000.00"))
                            .build(),

                    Customer.builder()
                            .accountNumber("100003")
                            .fullName("Saman")
                            .email("saman@example.com")
                            .balance(new BigDecimal("10000.00"))
                            .build()
            );

            customerRepository.saveAll(customers);
        }
    }
}