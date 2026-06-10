package com.foodredistribution.foodredistribution.exception;

public class InsufficientQuantityException
        extends RuntimeException {

    public InsufficientQuantityException(
            String message
    ) {
        super(message);
    }
}