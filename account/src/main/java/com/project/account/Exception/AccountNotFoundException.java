package com.project.account.Exception;


public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String msg) {
        super(msg);
    }
}