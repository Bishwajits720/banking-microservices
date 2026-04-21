package com.project.account.Controller;

import com.project.account.DTO.Response.AccountResponse;
import com.project.account.DTO.Response.ApiResponse;
import com.project.account.Entity.Account;
import com.project.account.Service.AccountService;
import com.sun.istack.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/get")
    public ResponseEntity<?> createAccount(@RequestParam @NotBlank(message = "Name cannot be empty") String name) {
        Account account = accountService.createAccount(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(AccountResponse.from(account)));
    }

    @GetMapping("/getBy/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable @NotBlank String accountNumber) throws AccountNotFoundException {
        Account account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(account)));
    }

    @PostMapping("/debit")
    public ResponseEntity<?> debit(@RequestParam @NotBlank String accountNumber, @RequestParam @NotNull @Positive BigDecimal amount) throws AccountNotFoundException {
        Account account = accountService.updateBalance(accountNumber, amount, true);
        return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(account)));
    }

    @PostMapping("/credit")
    public ResponseEntity<?> credit(@RequestParam @NotBlank String accountNumber, @RequestParam @NotNull @Positive BigDecimal amount) throws AccountNotFoundException {
        Account account = accountService.updateBalance(accountNumber, amount, false);
        return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(account)));
    }
}