package com.space.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * TODO: add documentation
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShipNotFoundException extends RuntimeException {
    public ShipNotFoundException(String message) {
        super(message);
    }
}
