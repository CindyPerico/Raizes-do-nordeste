package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.PaginaResponse;
import br.com.cindyperico.raizesdonordeste.dto.pedido.*;
import br.com.cindyperico.raizesdonordeste.model.Pedido;
import br.com.cindyperico.raizesdonordeste.model.enums.CanalAtendimento;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;
import br.com.cindyperico.raizesdonordeste.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Fluxo crítico: criação do pedido, pagamento mock e atualização de status")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Cria um pedido com itens, exigindo o canal de origem (canalPedido)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido (ex.: canalPedido ausente)"),
            @ApiResponse(responseCode = "404", description = "Unidade, cliente ou produto inexistente"),
            @ApiResponse(responseCode = "409", description = "Estoque insuficiente ou produto indisponível")
    })
    public ResponseEntity<PedidoResumoResponse> create(HttpServletRequest request, @Valid @RequestBody PedidoCreateRequest dto) {
        Pedido saved = pedidoService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResumo(saved));
    }

    @GetMapping
    @Operation(summary = "Lista pedidos de forma paginada, com filtro por canal, status e unidade")
    public ResponseEntity<PaginaResponse<PedidoResumoResponse>> list(
            @RequestParam(required = false) CanalAtendimento canalPedido,
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Long unidadeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return ResponseEntity.ok(PaginaResponse.of(
                pedidoService.buscar(canalPedido, status, unidadeId, pageable), this::toResumo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um pedido pelo identificador")
    public ResponseEntity<PedidoResumoResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResumo(pedidoService.get(id)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualiza o status do pedido (cozinha, pronto, finalizado)")
    public ResponseEntity<PedidoResumoResponse> updateStatus(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoStatusUpdateRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.updateStatus(request, id, dto)));
    }

    @PostMapping("/{id}/pagamento/solicitar")
    @Operation(summary = "Solicita o pagamento ao gateway externo simulado (mock) e gera a referência externa")
    public ResponseEntity<PedidoResumoResponse> solicitarPagamento(HttpServletRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(toResumo(pedidoService.solicitarPagamento(request, id)));
    }

    @PostMapping("/{id}/pagamento/confirmar")
    @Operation(summary = "Recebe o retorno do gateway mock: aprovado baixa estoque e credita pontos; recusado mantém o pedido")
    public ResponseEntity<PedidoResumoResponse> confirmarPagamento(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PagamentoConfirmacaoRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.confirmarPagamento(request, id, dto)));
    }

    @PostMapping("/{id}/desconto")
    @Operation(summary = "Aplica desconto promocional ao pedido")
    public ResponseEntity<PedidoResumoResponse> desconto(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoDescontoRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.aplicarDesconto(request, id, dto)));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancela o pedido registrando o motivo")
    public ResponseEntity<PedidoResumoResponse> cancelar(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoCancelarRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.cancelar(request, id, dto)));
    }

    @PostMapping("/{id}/ajuste-pontos")
    @Operation(summary = "Ajusta manualmente os pontos de fidelidade do cliente do pedido")
    public ResponseEntity<Void> ajustePontos(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoAjusteRequest dto) {
        pedidoService.ajustarPontosCliente(request, id, dto);
        return ResponseEntity.noContent().build();
    }

    private PedidoResumoResponse toResumo(Pedido p) {
        PedidoResumoResponse r = new PedidoResumoResponse();
        r.setId(p.getId());
        r.setClienteId(p.getCliente() != null ? p.getCliente().getId() : null);
        r.setUnidadeId(p.getUnidade() != null ? p.getUnidade().getId() : null);
        r.setCanalPedido(p.getCanalPedido());
        r.setStatus(p.getStatus());
        r.setStatusPagamentoExterno(p.getStatusPagamentoExterno());
        r.setReferenciaPagamentoExterno(p.getReferenciaPagamentoExterno());
        r.setCriadoEm(p.getCriadoEm());
        r.setSubtotal(p.getSubtotal());
        r.setDesconto(p.getDesconto());
        r.setTotal(p.getTotal());
        return r;
    }
}
