package com.project.transaction.Service;

import com.project.transaction.Entity.Transaction;
import com.project.transaction.Enum.Status;
import com.project.transaction.Exception.DuplicateTransactionException;
import com.project.transaction.Exception.InvalidRequestException;
import com.project.transaction.Exception.TransactionFailedException;
import com.project.transaction.FeignClient.AccountClient;
import com.project.transaction.Repository.TransactionRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private TransactionService service;

    @Test
    void shouldDepositSuccessfully() {

        when(repository.findByTransactionId("txn-1"))
                .thenReturn(Optional.empty());

        when(repository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Transaction txn = service.deposit("txn-1", "123", new BigDecimal("100"));

        assertEquals(Status.SUCCESS, txn.getStatus());
    }

    @Test
    void shouldWithdrawSuccessfully() {

        when(repository.findByTransactionId("txn-2"))
                .thenReturn(Optional.empty());

        when(repository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Transaction txn = service.withdraw("txn-2", "123", new BigDecimal("50"));

        assertEquals(Status.SUCCESS, txn.getStatus());
    }

    @Test
    void shouldFailWhenInsufficientBalance() {

        when(repository.findByTransactionId("txn-3"))
                .thenReturn(Optional.empty());

        doThrow(FeignException.BadRequest.class)
                .when(accountClient)
                .debit(anyString(), any());

        assertThrows(TransactionFailedException.class, () -> {
            service.withdraw("txn-3", "123", new BigDecimal("100"));
        });
    }

    @Test
    void shouldThrowWhenDuplicateTxn() {

        Transaction existing = new Transaction();

        when(repository.findByTransactionId("txn-4"))
                .thenReturn(Optional.of(existing));

        assertThrows(DuplicateTransactionException.class, () -> {
            service.deposit("txn-4", "123", new BigDecimal("100"));
        });
    }

    @Test
    void shouldFailWhenInvalidAmount() {

        assertThrows(InvalidRequestException.class, () -> {
            service.deposit("txn-5", "123", BigDecimal.ZERO);
        });
    }

    @Test
    void shouldTransferSuccessfully() {

        when(repository.findByTransactionId("txn-6"))
                .thenReturn(Optional.empty());

        when(repository.save(any(Transaction.class)))
                .thenAnswer(i -> i.getArgument(0));

        Transaction txn = service.transfer("txn-6", "123", "456", new BigDecimal("50"));

        assertEquals(Status.SUCCESS, txn.getStatus());
    }

    @Test
    void shouldRollbackWhenCreditFails() {

        when(repository.findByTransactionId("txn-7"))
                .thenReturn(Optional.empty());

        doNothing().when(accountClient).debit(anyString(), any());

        doThrow(FeignException.class)
                .when(accountClient)
                .credit(eq("456"), any());

        assertThrows(TransactionFailedException.class, () -> {
            service.transfer("txn-7", "123", "456", new BigDecimal("50"));
        });
    }


    @Test
    void shouldRejectDuplicateTxn() {

        Transaction existing = new Transaction();

        when(repository.findByTransactionId("txn-9"))
                .thenReturn(Optional.of(existing));

        assertThrows(DuplicateTransactionException.class, () -> {
            service.deposit("txn-9", "123", new BigDecimal("50"));
        });
    }

}