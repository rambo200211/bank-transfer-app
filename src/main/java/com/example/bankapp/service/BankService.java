package com.example.bankapp.service;
import org.springframework.transaction.annotation.Propagation;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;

import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final TransactionAuditService transactionAuditService;


 

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

       
        if (fromAccount == null || fromAccount.isBlank()
                || toAccount == null || toAccount.isBlank()
                || amount == null) {

            throw new IllegalArgumentException(
                    "Please provide valid transfer details"
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        try {

           
            if (fromAccount.equals(toAccount)) {
                throw new IllegalArgumentException(
                        "Sender and receiver accounts must be different"
                );
            }

           
            Customer sender = customerRepository
                    .findByAccountNumber(fromAccount)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Sender account not found"
                            )
                    );

       
            Customer receiver = customerRepository
                    .findByAccountNumber(toAccount)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Receiver account not found"
                            )
                    );

     
            if (sender.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient balance"
                );
            }

         
            sender.setBalance(
                    sender.getBalance().subtract(amount)
            );

       
            receiver.setBalance(
                    receiver.getBalance().add(amount)
            );

       
            customerRepository.save(sender);
            customerRepository.save(receiver);

    
            if (simulateFailure) {
                throw new RuntimeException(
                        "Simulated transfer failure"
                );
            }

            BankTransaction transaction = new BankTransaction();

            transaction.setFromAccount(fromAccount);
            transaction.setToAccount(toAccount);
            transaction.setAmount(amount);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setDescription(
                    "Money transferred successfully"
            );
            transaction.setCreatedAt(LocalDateTime.now());

            return bankTransactionRepository.save(transaction);

        } catch (RuntimeException exception) {

           
            try {
                transactionAuditService.recordFailure(
                        fromAccount,
                        toAccount,
                        amount,
                        exception.getMessage()
                );
            } catch (RuntimeException auditException) {
              
                exception.addSuppressed(auditException);
            }

  
            throw exception;
        }
    }


    

    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.existsByAccountNumber(
                customer.getAccountNumber())) {

            throw new IllegalArgumentException(
                    "Account number already exists"
            );
        }

        return customerRepository.save(customer);
    }


   

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {

        return customerRepository.findAll();
    }




    @Transactional(readOnly = true)
    public Customer getCustomer(String accountNumber) {

        return customerRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found"
                        )
                );
    }



    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String keyword) {

        return customerRepository.searchCustomers(keyword);
    }




    @Transactional(readOnly = true)
    public List<BankTransaction> getAllTransactions() {

        return bankTransactionRepository.findAllLatest();
    }


    

    @Transactional(readOnly = true)
    public List<BankTransaction> getLatestTransactions() {

        return bankTransactionRepository.findAllLatest();
    }


 

    @Transactional(readOnly = true)
    public List<BankTransaction> getTransactionHistory(
            String accountNumber) {

        return bankTransactionRepository.findHistory(accountNumber);
    }




    @Transactional(readOnly = true)
    public List<BankTransaction> getAccountHistory(
            String accountNumber) {

        return bankTransactionRepository.findHistory(accountNumber);
    }



    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance() {

        return customerRepository.getTotalBalance();
    }



    @Transactional(readOnly = true)
    public long getSuccessCount() {

        return bankTransactionRepository.countByStatus(
                TransactionStatus.SUCCESS
        );
    }




    @Transactional(readOnly = true)
    public long getFailedCount() {

        return bankTransactionRepository.countByStatus(
                TransactionStatus.FAILED
        );
    }




    @Transactional(readOnly = true)
    public List<BankTransaction> getTransactionsByStatus(
            TransactionStatus status) {

        return bankTransactionRepository.findByStatusLatest(status);
    }



    @Transactional(readOnly = true)
    public long countTransactionsByStatus(
            TransactionStatus status) {

        return bankTransactionRepository.countByStatus(status);
    }

}