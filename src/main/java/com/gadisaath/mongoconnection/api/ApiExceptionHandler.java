package com.gadisaath.mongoconnection.api;

import com.gadisaath.mongoconnection.service.MongoUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MongoUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleMongoUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "MongoDB unavailable",
                        "message", "Verify MongoDB is running and MONGODB_URI points to a reachable server."
                ));
    }
}