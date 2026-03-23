package com.Major.majorProject.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException ex, HttpServletRequest request, Model model) {
        if (isApiRequest(request)) {
            return buildApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        }

        model.addAttribute("statusCode", HttpStatus.BAD_REQUEST.value());
        model.addAttribute("errorTitle", "Something went wrong");
        model.addAttribute("errorMessage", safeMessage(ex.getMessage()));
        model.addAttribute("requestPath", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, HttpServletRequest request, Model model) {
        if (isApiRequest(request)) {
            return buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error. Please try again.", request.getRequestURI());
        }

        model.addAttribute("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage", "Something unexpected happened. Please go back and try again.");
        model.addAttribute("requestPath", request.getRequestURI());
        return "error";
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api");
    }

    private ResponseEntity<Map<String, Object>> buildApiError(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", safeMessage(message));
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank()
                ? "Something went wrong. Please try again."
                : message;
    }
}
