package br.com.cindyperico.raizesdonordeste;

import br.com.cindyperico.raizesdonordeste.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutenticacaoTest extends ApiTestSupport {

    @Test
    @DisplayName("T01 - login com credenciais válidas devolve token JWT")
    void loginValido() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("email", EMAIL_ADMIN)
                .put("senha", SENHA_ADMIN)
                .toString();

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("T02 - requisição sem token retorna 401 com erro padronizado")
    void semToken() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/pedidos"));
    }

    @Test
    @DisplayName("T03 - perfil CLIENTE não acessa relatórios administrativos (403)")
    void clienteSemPermissao() throws Exception {
        mockMvc.perform(get("/api/relatorios/financeiro").header(HttpHeaders.AUTHORIZATION, bearer(tokenCliente)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("T04 - login com senha incorreta retorna 401")
    void loginInvalido() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("email", EMAIL_ADMIN)
                .put("senha", "senha-errada")
                .toString();

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }
}
