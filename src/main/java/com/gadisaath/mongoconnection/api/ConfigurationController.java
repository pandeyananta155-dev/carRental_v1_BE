package com.gadisaath.mongoconnection.api;

import com.gadisaath.mongoconnection.service.MongoConfigurationService;
import com.gadisaath.mongoconnection.service.MongoUnavailableException;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ConfigurationController {
    private final MongoConfigurationService configurationService;

    public ConfigurationController(MongoConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try {
            configurationService.ping();
            return ResponseEntity.ok(Map.of("apiStatus", "UP", "mongodbStatus", "CONNECTED"));
        } catch (MongoUnavailableException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("apiStatus", "UP", "mongodbStatus", "UNAVAILABLE"));
        }
    }

    @GetMapping("/config/{typeCode}")
    public ResponseEntity<Document> getConfiguration(@PathVariable String typeCode) {
        Optional<Document> configuration = configurationService.findByTypeCode(typeCode);
        return configuration.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}