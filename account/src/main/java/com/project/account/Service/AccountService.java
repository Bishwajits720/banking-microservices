package com.project.account.Service;

import com.project.account.Entity.Account;
import com.project.account.Enum.Status;
import com.project.account.Exception.AccountBlockedException;
import com.project.account.Exception.ConcurrentUpdateException;
import com.project.account.Exception.InsufficientBalanceException;
import com.project.account.Exception.InvalidRequestException;
import com.project.account.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("Customer name cannot be empty");
        }
        Account acc = new Account();
        acc.setAccountNumber(UUID.randomUUID().toString());
        acc.setCustomerName(name.trim());
        acc.setBalance(BigDecimal.ZERO);
        acc.setStatus(Status.ACTIVE);
        acc.setCreatedAt(LocalDateTime.now());
        log.info("Creating account for customer: {} , Account Number {} ", name ,acc.getAccountNumber());
        return accountRepository.save(acc);
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException {

        validateAccountNumber(accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found: {}", accountNumber);
                    return new AccountNotFoundException("Account not found");
                });
    }

    @Transactional
    public Account updateBalance(String accountNumber, BigDecimal amount, boolean isDebit) throws AccountNotFoundException {

        validateAccountNumber(accountNumber);
        validateAmount(amount);

        Account acc = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found: {}", accountNumber);
                    return new AccountNotFoundException("Account not found");
                });

        validateAccountStatus(acc);

        if (isDebit) {
            validateSufficientBalance(acc, amount);
            acc.setBalance(acc.getBalance().subtract(amount));
            log.info("Debited {} from account {}", amount, accountNumber);
        } else {
            acc.setBalance(acc.getBalance().add(amount));
            log.info("Credited {} to account {}", amount, accountNumber);
        }

        try {
            return accountRepository.save(acc);
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.error("Concurrent update detected for account {}", accountNumber);
            throw new ConcurrentUpdateException("Concurrent transaction detected, please retry");
        }
    }

    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new InvalidRequestException("Account number cannot be empty");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than zero");
        }
    }

    private void validateAccountStatus(Account acc) {
        if (acc.getStatus() != Status.ACTIVE) {
            throw new AccountBlockedException("Account is blocked");
        }
    }

    private void validateSufficientBalance(Account acc, BigDecimal amount) {
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }

}
