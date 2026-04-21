package com.project.transaction.DTO.Response;

import com.project.transaction.Entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class AccountResponse {

    private String accountNumber;
    private String customerName;
    private BigDecimal balance;
    private String status;

    public static AccountResponse from(Account acc) {
        return AccountResponse.builder()
                .accountNumber(acc.getAccountNumber())
                .customerName(acc.getCustomerName())
                .balance(acc.getBalance())
                .status(acc.getStatus().name())
                .build();
    }
}
