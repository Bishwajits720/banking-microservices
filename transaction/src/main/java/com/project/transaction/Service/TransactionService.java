package com.project.transaction.Service;

import com.project.transaction.Entity.Transaction;
import com.project.transaction.Enum.Status;
import com.project.transaction.Enum.Type;
import com.project.transaction.Exception.DuplicateTransactionException;
import com.project.transaction.Exception.ExternalServiceException;
import com.project.transaction.Exception.InvalidRequestException;
import com.project.transaction.Exception.TransactionFailedException;
import com.project.transaction.FeignClient.AccountClient;
import com.project.transaction.Repository.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountClient accountClient;

    public Transaction deposit(String txnId, String account, BigDecimal amount) {

        validateRequest(txnId, account, amount);

        Optional<Transaction> existing = repository.findByTransactionId(txnId);
        if (existing.isPresent()) {
            log.warn("Duplicate transaction detected: {}", txnId);
            throw new DuplicateTransactionException("Transaction already processed");
        }
        Transaction txn = buildTxn(txnId, account, null, amount, Type.DEPOSIT);
        try {
            accountClient.credit(account, amount);
            txn.setStatus(Status.SUCCESS);
            log.info("Deposit SUCCESS txnId={}", txnId);
        } catch (FeignException.BadRequest ex) {
            txn.setStatus(Status.FAILED);
            log.error("Deposit failed (business error) txnId={} error={}", txnId, ex.contentUTF8());
            throw new TransactionFailedException("Invalid deposit request");
        } catch (FeignException ex) {
            txn.setStatus(Status.FAILED);
            log.error("Account service error txnId={}", txnId, ex);
            throw new ExternalServiceException("Account service unavailable");
        }
        return repository.save(txn);
    }

    public Transaction withdraw(String txnId, String account, BigDecimal amount) {

        validateRequest(txnId, account, amount);

        Optional<Transaction> existing = repository.findByTransactionId(txnId);
        if (existing.isPresent()) {
            log.warn("Duplicate transaction detected: {}", txnId);
            throw new DuplicateTransactionException("Transaction already processed");
        }

        Transaction txn = buildTxn(txnId, account, null, amount, Type.WITHDRAW);
        try {
            accountClient.debit(account, amount);
            txn.setStatus(Status.SUCCESS);
            log.info("Withdraw SUCCESS txnId={}", txnId);
        } catch (FeignException.BadRequest ex) {
            txn.setStatus(Status.FAILED);
            log.error("Withdraw failed (business error) txnId={} error={}", txnId, ex.contentUTF8());
            throw new TransactionFailedException("Insufficient balance");
        } catch (FeignException ex) {
            txn.setStatus(Status.FAILED);
            log.error("Account service error txnId={}", txnId, ex);
            throw new ExternalServiceException("Account service unavailable");
        }
        return repository.save(txn);
    }

    public Transaction transfer(String txnId, String from, String to, BigDecimal amount) {

        validateRequest(txnId, from, amount);

        if (to == null || to.isBlank()) {
            throw new InvalidRequestException("Target account required");
        }

        if (from.equals(to)) {
            throw new InvalidRequestException("Source and target cannot be same");
        }

        Optional<Transaction> existing = repository.findByTransactionId(txnId);
        if (existing.isPresent()) {
            log.warn("Duplicate transaction detected: {}", txnId);
            throw new DuplicateTransactionException("Transaction already processed");
        }
        Transaction txn = buildTxn(txnId, from, to, amount, Type.TRANSFER);
        try {
            accountClient.debit(from, amount);
            try {
                accountClient.credit(to, amount);
                txn.setStatus(Status.SUCCESS);
                log.info("Transfer SUCCESS txnId={}", txnId);
            } catch (FeignException creditEx) {
                log.error("Credit failed → rolling back debit txnId={}", txnId);
                try {
                    accountClient.credit(from, amount); // refund
                    log.info("Rollback successful txnId={}", txnId);
                } catch (Exception rollbackEx) {
                    log.error("CRITICAL: rollback failed txnId={}", txnId, rollbackEx);
                }
                txn.setStatus(Status.FAILED);
                throw new TransactionFailedException("Transfer failed during credit");
            }
        } catch (FeignException.BadRequest ex) {
            log.error("Debit failed due to business rule txnId={} error={}", txnId, ex.contentUTF8());
            txn.setStatus(Status.FAILED);
            throw new TransactionFailedException("Insufficient balance");
        } catch (FeignException ex) {
            log.error("Account service error txnId={}", txnId, ex);
            txn.setStatus(Status.FAILED);
            throw new ExternalServiceException("Account service unavailable");
        }
        return repository.save(txn);
    }

    private void validateRequest(String txnId, String account, BigDecimal amount) {

        if (txnId == null || txnId.isBlank()) {
            throw new InvalidRequestException("Transaction ID required");
        }

        if (account == null || account.isBlank()) {
            throw new InvalidRequestException("Account required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than zero");
        }
    }

    private Transaction buildTxn(String txnId, String from, String to, BigDecimal amount, Type type) {
        Transaction txn = new Transaction();
        txn.setTransactionId(txnId);
        txn.setSourceAccount(from);
        txn.setTargetAccount(to);
        txn.setAmount(amount);
        txn.setType(type);
        txn.setCreatedAt(LocalDateTime.now());
        return txn;
    }
}