package com.project.transaction.DTO.Response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TransactionResponse {
    private String transactionId;
    private String status;
    private BigDecimal amount;
}