package com.project.transaction.FeignClient;

import com.project.transaction.Config.FeignConfig;
import com.project.transaction.DTO.Response.AccountResponse;
import com.project.transaction.DTO.Response.ApiResponse;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", url = "http://localhost:8081",configuration = FeignConfig.class )
@Retryable(value = FeignException.class, maxAttempts = 3)
public interface AccountClient {

    @PostMapping("/accounts/debit")
    ApiResponse<AccountResponse> debit(@RequestParam String accountNumber, @RequestParam BigDecimal amount);

    @PostMapping("/accounts/credit")
    ApiResponse<AccountResponse> credit(@RequestParam String accountNumber, @RequestParam BigDecimal amount);
}