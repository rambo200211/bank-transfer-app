
package com.example.bankapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    @NotBlank(message = "Please select a sender account")
    private String fromAccount;

    @NotBlank(message = "Please select a receiver account")
    private String toAccount;

    @NotNull(message = "Please enter an amount")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Enter a valid amount with up to two decimal places")
    private BigDecimal amount;

    private boolean simulateFailure;
}
