package br.com.cindyperico.raizesdonordeste.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiTestSupport {

    protected static final String EMAIL_ADMIN = "admin@raizes.com";
    protected static final String SENHA_ADMIN = "admin12345";
    protected static final String EMAIL_CLIENTE = "maria.souza@example.com";
    protected static final String SENHA_CLIENTE = "cliente12345";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String tokenAdmin;
    protected String tokenCliente;

    @BeforeEach
    void autenticar() throws Exception {
        tokenAdmin = login(EMAIL_ADMIN, SENHA_ADMIN);
        tokenCliente = login(EMAIL_CLIENTE, SENHA_CLIENTE);
    }

    protected String login(String email, String senha) throws Exception {
        String body = objectMapper.createObjectNode()
                .put("email", email)
                .put("senha", senha)
                .toString();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return json(result).get("accessToken").asText();
    }

    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
