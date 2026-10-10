package com.maxchauo.backendapilanguagelab.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps application failures to the shared API error format. */
@RestControllerAdvice
public class JobsExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobsExceptionHandler.class);

    /** Returns HTTP 400 for invalid query parameters. */
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(
            InvalidParameterException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(new ErrorDetails("INVALID_PARAMETER", exception.getMessage())));
    }

    /** Hides database details behind a generic HTTP 500 response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        LOGGER.error("List jobs failed", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(new ErrorDetails(
                        "INTERNAL_SERVER_ERROR", "failed to query jobs")));
    }
}
