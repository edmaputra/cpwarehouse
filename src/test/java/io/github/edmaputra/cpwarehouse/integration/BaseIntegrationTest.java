package io.github.edmaputra.cpwarehouse.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.edmaputra.cpwarehouse.CpwarehouseApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = CpwarehouseApplication.class)
@AutoConfigureMockMvc
//@Testcontainers
public class BaseIntegrationTest {

  //    @Container
  //    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
  //            .withExposedPorts(27017);

  //    @DynamicPropertySource
  //    static void setProperties(DynamicPropertyRegistry registry) {
  //        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  //    }

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;
}
