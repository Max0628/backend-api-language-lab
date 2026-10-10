package com.maxchauo.backendapilanguagelab.jobs;

/** Indicates that a jobs API query parameter is invalid. */
public class InvalidParameterException extends RuntimeException {
    /** Creates an exception with a client-safe validation message. */
    public InvalidParameterException(String message) {
        super(message);
    }
}
