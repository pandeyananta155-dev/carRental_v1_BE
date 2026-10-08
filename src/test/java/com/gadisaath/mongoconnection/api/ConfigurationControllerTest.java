package com.gadisaath.mongoconnection.api;

import com.gadisaath.mongoconnection.service.MongoConfigurationService;
import com.gadisaath.mongoconnection.service.MongoUnavailableException;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies HTTP responses from the configuration API with the database service mocked. */
@WebMvcTest(ConfigurationController.class)
class ConfigurationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MongoConfigurationService configurationService;

        /** Confirms a successful MongoDB ping produces a healthy response. */
    @Test
    void healthReportsMongoConnectedAfterSuccessfulPing() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiStatus").value("UP"))
                .andExpect(jsonPath("$.mongodbStatus").value("CONNECTED"));
    }

        /** Confirms a failed MongoDB ping is reported as service unavailable. */
    @Test
    void healthReturnsServiceUnavailableWhenMongoPingFails() throws Exception {
        doThrow(new MongoUnavailableException(new RuntimeException("offline")))
                .when(configurationService).ping();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mongodbStatus").value("UNAVAILABLE"));
    }

        /** Confirms the endpoint returns the document found by its exact type code. */
    @Test
    void returnsConfigurationByExactTypeCode() throws Exception {
        Document configuration = new Document("typeCode", "VEHICLE_SERVICE")
                .append("defaultServiceRadiusKm", 15);
        given(configurationService.findByTypeCode("VEHICLE_SERVICE"))
                .willReturn(Optional.of(configuration));

        mockMvc.perform(get("/api/config/VEHICLE_SERVICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCode").value("VEHICLE_SERVICE"))
                .andExpect(jsonPath("$.defaultServiceRadiusKm").value(15));
    }

        /** Confirms an unknown type code produces HTTP 404. */
    @Test
    void returnsNotFoundWhenConfigurationDoesNotExist() throws Exception {
        given(configurationService.findByTypeCode("UNKNOWN"))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/api/config/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

        /** Confirms a database query failure returns HTTP 503 with troubleshooting details. */
    @Test
    void returnsUsefulServiceUnavailableResponseWhenMongoQueryFails() throws Exception {
        given(configurationService.findByTypeCode("VEHICLE_SERVICE"))
                .willThrow(new MongoUnavailableException(new RuntimeException("offline")));

        mockMvc.perform(get("/api/config/VEHICLE_SERVICE"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("MongoDB unavailable"))
                .andExpect(jsonPath("$.message").exists());
    }
}