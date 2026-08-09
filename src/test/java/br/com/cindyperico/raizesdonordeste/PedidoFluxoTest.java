package br.com.cindyperico.raizesdonordeste;

import com.fasterxml.jackson.databind.JsonNode;
import br.com.cindyperico.raizesdonordeste.repository.AuditLogRepository;
import br.com.cindyperico.raizesdonordeste.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PedidoFluxoTest extends ApiTestSupport {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("T05 - cardapio da unidade lista produtos disponiveis com preco e estoque")
    void cardapioPorUnidade() throws Exception {
        mockMvc.perform(get("/api/unidades/1/produtos").header(HttpHeaders.AUTHORIZATION, bearer(tokenCliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoId").isNumber())
                .andExpect(jsonPath("$[0].preco").isNumber());
    }

    @Test
    @DisplayName("T06 - fluxo completo: pedido criado, pagamento mock aprovado, estoque baixado e status atualizado")
    void fluxoPedidoPagamentoStatus() throws Exception {
        int estoqueInicial = estoqueDoProduto(1L, 1L);

        long pedidoId = criarPedido("TOTEM", 1L, 2).get("id").asLong();

        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/pagamento/solicitar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                .andExpect(jsonPath("$.statusPagamentoExterno").value("SOLICITADO"))
                .andExpect(jsonPath("$.referenciaPagamentoExterno").isNotEmpty());

        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/pagamento/confirmar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmado\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.statusPagamentoExterno").value("CONFIRMADO"));

        assertThat(estoqueDoProduto(1L, 1L)).isEqualTo(estoqueInicial - 2);

        mockMvc.perform(put("/api/pedidos/" + pedidoId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"EM_PREPARO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }

    @Test
    @DisplayName("T07 - pagamento mock recusado mantem o pedido sem baixa de estoque")
    void pagamentoRecusado() throws Exception {
        int estoqueInicial = estoqueDoProduto(1L, 3L);
        long pedidoId = criarPedido("APP", 3L, 1).get("id").asLong();

        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/pagamento/solicitar")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))).andExpect(status().isOk());

        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/pagamento/confirmar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmado\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamentoExterno").value("RECUSADO"))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"));

        assertThat(estoqueDoProduto(1L, 3L)).isEqualTo(estoqueInicial);
    }

    @Test
    @DisplayName("T08 - listagem paginada filtra pedidos por canalPedido")
    void filtrarPorCanal() throws Exception {
        criarPedido("WEB", 3L, 1);

        MvcResult result = mockMvc.perform(get("/api/pedidos")
                        .param("canalPedido", "WEB")
                        .param("size", "50")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andReturn();

        JsonNode content = json(result).get("content");
        assertThat(content).isNotEmpty();
        content.forEach(pedido -> assertThat(pedido.get("canalPedido").asText()).isEqualTo("WEB"));
    }

    @Test
    @DisplayName("T09 - pedido sem canalPedido retorna 400 com erro padronizado")
    void pedidoSemCanal() throws Exception {
        String body = "{\"unidadeId\": 1, \"itens\": [{\"produtoId\": 1, \"quantidade\": 1}]}";

        mockMvc.perform(post("/api/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("canalPedido")));
    }

    @Test
    @DisplayName("T10 - pedido com produto inexistente retorna 404")
    void pedidoProdutoInexistente() throws Exception {
        String body = "{\"canalPedido\": \"APP\", \"unidadeId\": 1, \"itens\": [{\"produtoId\": 9999, \"quantidade\": 1}]}";

        mockMvc.perform(post("/api/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("T11 - pedido com estoque insuficiente retorna 409")
    void pedidoEstoqueInsuficiente() throws Exception {
        String body = "{\"canalPedido\": \"BALCAO\", \"unidadeId\": 1, \"itens\": [{\"produtoId\": 2, \"quantidade\": 9999}]}";

        mockMvc.perform(post("/api/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente"));
    }

    @Test
    @DisplayName("T12 - criacao de pedido gera registro de auditoria")
    void auditoriaDoPedido() throws Exception {
        long antes = auditLogRepository.count();
        criarPedido("PICKUP", 3L, 1);

        assertThat(auditLogRepository.count()).isGreaterThan(antes);
        assertThat(auditLogRepository.findAll().stream()
                .anyMatch(log -> "PEDIDO_CRIADO".equals(log.getAcao()))).isTrue();
    }

    private JsonNode criarPedido(String canalPedido, long produtoId, int quantidade) throws Exception {
        String body = "{\"canalPedido\": \"" + canalPedido + "\", \"clienteId\": 1, \"unidadeId\": 1, "
                + "\"itens\": [{\"produtoId\": " + produtoId + ", \"quantidade\": " + quantidade + "}]}";

        MvcResult result = mockMvc.perform(post("/api/pedidos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.canalPedido").value(canalPedido))
                .andReturn();

        return json(result);
    }

    private int estoqueDoProduto(long unidadeId, long produtoId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/unidades/" + unidadeId + "/estoque/produtos/" + produtoId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andReturn();

        return json(result).get("quantidade").asInt();
    }
}
