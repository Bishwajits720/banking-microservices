package com.project.transaction.Controller;

import com.project.transaction.DTO.Request.TransactionRequest;
import com.project.transaction.DTO.Response.ApiResponse;
import com.project.transaction.DTO.Response.TransactionResponse;
import com.project.transaction.Entity.Transaction;
import com.project.transaction.Exception.InvalidRequestException;
import com.project.transaction.Service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody TransactionRequest request) {
        Transaction txn = transactionService.deposit(request.getTxnId(), request.getAccount(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(buildResponse(txn)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@Valid @RequestBody TransactionRequest request) {
        Transaction txn = transactionService.withdraw(request.getTxnId(), request.getAccount(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(buildResponse(txn)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransactionRequest request) {

        if (request.getTargetAccount() == null || request.getTargetAccount().isBlank()) {
            throw new InvalidRequestException("Target account required");
        }
        Transaction txn = transactionService.transfer(request.getTxnId(), request.getAccount(), request.getTargetAccount(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(buildResponse(txn)));
    }

    private TransactionResponse buildResponse(Transaction txn) {
        return TransactionResponse.builder().transactionId(txn.getTransactionId())
                .status(txn.getStatus().name())
                .amount(txn.getAmount())
                .build();
    }
}