package com.project.account.Exception;

public class ConcurrentUpdateException extends RuntimeException {
    public ConcurrentUpdateException(String msg) { super(msg); }
}