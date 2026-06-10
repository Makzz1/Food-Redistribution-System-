package com.foodredistribution.foodredistribution.exception;

public class EmailNotVerifiedException
        extends RuntimeException {

    public EmailNotVerifiedException(
            String message
    ) {
        super(message);
    }
}