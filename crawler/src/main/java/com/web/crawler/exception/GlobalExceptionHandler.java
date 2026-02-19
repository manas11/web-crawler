package com.web.crawler.exception;

import com.web.crawler.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.ConnectException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles IllegalArgumentException (invalid URL format).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidInput(IllegalArgumentException ex) {
        log.error("IllegalArgumentException caught: {}", ex.getMessage());
        log.error("SUGGESTION: Ensure URL starts with http:// or https://");
        return new ErrorResponse("invalid", ex.getMessage());
    }

    /**
     * Handles SocketTimeoutException (website too slow to respond).
     */
    @ExceptionHandler(SocketTimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public ErrorResponse handleSocketTimeout(SocketTimeoutException ex) {
        log.error("SocketTimeoutException caught: Website took too long to respond");
        log.error("SUGGESTIONS:");
        log.error("  1. Increase timeout values in application.properties (crawler.timeout.*)");
        log.error("  2. Website may be overloaded - try again later");
        log.error("  3. Check your network connection");
        return new ErrorResponse("timeout", "Website took too long to respond. Increase timeout and retry.");
    }

    /**
     * Handles ConnectException (website blocking or connection refused).
     */
    @ExceptionHandler(ConnectException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleConnectionError(ConnectException ex) {
        log.error("ConnectException caught: Website refused connection or is blocking requests");
        log.error("SUGGESTIONS:");
        log.error("  1. Website may be blocking automated requests");
        log.error("  2. Increase delay between requests (crawler.retry.delay.ms in application.properties)");
        log.error("  3. Check if website has robots.txt restrictions");
        log.error("  4. Verify the URL is accessible from your network");
        return new ErrorResponse("blocked", "Failed to connect. Website may be blocking requests.");
    }

    /**
     * Handles generic IOException (network/fetch errors).
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleIO(IOException ex) {
        log.error("IOException caught: {}", ex.getMessage());

        String message = ex.getMessage();
        if (message != null && message.toLowerCase().contains("timeout")) {
            log.error("SUGGESTION: Request timeout - website is slow. Increase crawler.timeout.* values");
        } else if (message != null && message.toLowerCase().contains("connection")) {
            log.error("SUGGESTION: Connection error - website may be blocking. Check robots.txt and increase delays");
        }

        return new ErrorResponse("unknown", "Failed to fetch URL (timeout or blocked): " + message);
    }

    /**
     * Handles all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Unexpected exception caught: ", ex);
        log.error("Exception type: {}", ex.getClass().getName());
        log.error("Message: {}", ex.getMessage());
        return new ErrorResponse("unknown", "An unexpected error occurred: " + ex.getMessage());
    }
}