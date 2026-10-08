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

@WebMvcTest(ConfigurationController.class)
class ConfigurationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MongoConfigurationService configurationService;

    @Test
    void healthReportsMongoConnectedAfterSuccessfulPing() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiStatus").value("UP"))
                .andExpect(jsonPath("$.mongodbStatus").value("CONNECTED"));
    }

    @Test
    void healthReturnsServiceUnavailableWhenMongoPingFails() throws Exception {
        doThrow(new MongoUnavailableException(new RuntimeException("offline")))
                .when(configurationService).ping();

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mongodbStatus").value("UNAVAILABLE"));
    }

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

    @Test
    void returnsNotFoundWhenConfigurationDoesNotExist() throws Exception {
        given(configurationService.findByTypeCode("UNKNOWN"))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/api/config/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

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