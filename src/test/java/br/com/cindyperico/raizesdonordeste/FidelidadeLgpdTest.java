package br.com.cindyperico.raizesdonordeste;

import br.com.cindyperico.raizesdonordeste.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FidelidadeLgpdTest extends ApiTestSupport {

    @Test
    @DisplayName("T13 - fidelidade exige consentimento LGPD e respeita o saldo de pontos")
    void fidelidadeComConsentimento() throws Exception {
        long clienteId = criarCliente("Joana Lima", "joana.lima@example.com");

        mockMvc.perform(post("/api/clientes/" + clienteId + "/pontos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontos\": 10}"))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/clientes/" + clienteId + "/consentimento")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consentido\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lgpdConsentido").value(true));

        mockMvc.perform(post("/api/clientes/" + clienteId + "/pontos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontos\": 10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontosFidelidade").value(10));

        mockMvc.perform(post("/api/clientes/" + clienteId + "/pontos/resgatar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontos\": 50}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("T14 - anonimizacao remove dados pessoais do cliente")
    void anonimizacao() throws Exception {
        long clienteId = criarCliente("Carlos Dias", "carlos.dias@example.com");

        mockMvc.perform(post("/api/clientes/" + clienteId + "/anonimizar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonimizado").value(true))
                .andExpect(jsonPath("$.cpf").doesNotExist());
    }

    @Test
    @DisplayName("T15 - cadastro de cliente com e-mail invalido retorna 400")
    void emailInvalido() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Teste\", \"email\": \"nao-eh-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("T16 - listagem de clientes e paginada")
    void listagemPaginada() throws Exception {
        mockMvc.perform(get("/api/clientes")
                        .param("page", "0")
                        .param("size", "1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    private long criarCliente(String nome, String email) throws Exception {
        String body = objectMapper.createObjectNode()
                .put("nome", nome)
                .put("email", email)
                .toString();

        MvcResult result = mockMvc.perform(post("/api/clientes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return json(result).get("id").asLong();
    }
}
