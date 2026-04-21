package com.project.transaction.DTO.Request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    @NotBlank(message = "Account is required")
    private String account;

    private String targetAccount;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;
}