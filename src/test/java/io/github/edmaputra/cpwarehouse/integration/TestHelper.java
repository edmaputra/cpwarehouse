package io.github.edmaputra.cpwarehouse.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.edmaputra.cpwarehouse.dto.request.ItemCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class TestHelper {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    public String createTestItem(String sku, String name, BigDecimal basePrice) throws Exception {
        ItemCreateRequest request = ItemCreateRequest.builder()
                .sku(sku)
                .name(name)
                .description("Test description for " + name)
                .basePrice(basePrice)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andReturn();

        String responseContent = result.getResponse().getContentAsString();

        // Parse the response to extract the item ID
        JsonNode jsonNode = objectMapper.readTree(responseContent);

        return jsonNode.get("data").get("id").asText();
    }
}
