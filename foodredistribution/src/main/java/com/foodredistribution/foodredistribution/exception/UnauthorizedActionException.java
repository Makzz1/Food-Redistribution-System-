package com.foodredistribution.foodredistribution.exception;

public class UnauthorizedActionException
        extends RuntimeException {

    public UnauthorizedActionException(
            String message
    ) {
        super(message);
    }
}