package com.maxchauo.backendapilanguagelab.jobs;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Standard API error response. */
@Getter
@AllArgsConstructor
public class ApiError {
    private final ErrorDetails error;
}

/** Machine-readable API error details. */
@Getter
@AllArgsConstructor
class ErrorDetails {
    private final String code;
    private final String message;
}
