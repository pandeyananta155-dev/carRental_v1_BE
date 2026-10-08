package com.gadisaath.mongoconnection.api;

import com.gadisaath.mongoconnection.service.MongoUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Converts MongoDB availability failures into consistent HTTP 503 responses. */
@RestControllerAdvice
public class ApiExceptionHandler {
    /**
     * Returns a client-safe explanation when an endpoint cannot reach MongoDB.
     *
     * @return HTTP 503 with a brief error and local troubleshooting guidance
     */
    @ExceptionHandler(MongoUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleMongoUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "MongoDB unavailable",
                        "message", "Verify MongoDB is running and MONGODB_URI points to a reachable server."
                ));
    }
}