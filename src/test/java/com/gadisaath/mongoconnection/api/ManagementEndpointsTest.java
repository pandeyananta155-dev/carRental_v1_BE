package com.gadisaath.mongoconnection.api;

import com.gadisaath.mongoconnection.service.MongoConfigurationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "management.health.mongo.enabled=false")
@AutoConfigureMockMvc
class ManagementEndpointsTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MongoConfigurationService configurationService;

    @Test
    void exposesActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void exposesSwaggerUiAtConfiguredPath() throws Exception {
        mockMvc.perform(get("/swagger-ui"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("swagger-ui")));
    }

    @Test
    void exposesOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }
}
