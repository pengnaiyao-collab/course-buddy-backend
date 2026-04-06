package com.coursebuddy.common.exception;

/**
 * 资源异常
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
