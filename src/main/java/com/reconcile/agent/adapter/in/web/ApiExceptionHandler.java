package com.reconcile.agent.adapter.in.web;

import com.reconcile.agent.application.InvalidUploadException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(InvalidUploadException.class)
    ResponseEntity<Map<String, String>> invalidUpload(InvalidUploadException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
