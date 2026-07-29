package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.pedido.*;
import br.com.cindyperico.raizesdonordeste.model.Pedido;
import br.com.cindyperico.raizesdonordeste.service.PedidoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResumoResponse> create(HttpServletRequest request, @Valid @RequestBody PedidoCreateRequest dto) {
        Pedido saved = pedidoService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResumo(saved));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResumoResponse>> list() {
        return ResponseEntity.ok(pedidoService.list().stream().map(this::toResumo).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResumoResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResumo(pedidoService.get(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PedidoResumoResponse> updateStatus(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoStatusUpdateRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.updateStatus(request, id, dto)));
    }

    @PostMapping("/{id}/pagamento/solicitar")
    public ResponseEntity<PedidoResumoResponse> solicitarPagamento(HttpServletRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(toResumo(pedidoService.solicitarPagamento(request, id)));
    }

    @PostMapping("/{id}/pagamento/confirmar")
    public ResponseEntity<PedidoResumoResponse> confirmarPagamento(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PagamentoConfirmacaoRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.confirmarPagamento(request, id, dto)));
    }

    @PostMapping("/{id}/desconto")
    public ResponseEntity<PedidoResumoResponse> desconto(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoDescontoRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.aplicarDesconto(request, id, dto)));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResumoResponse> cancelar(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoCancelarRequest dto) {
        return ResponseEntity.ok(toResumo(pedidoService.cancelar(request, id, dto)));
    }

    @PostMapping("/{id}/ajuste-pontos")
    public ResponseEntity<Void> ajustePontos(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PedidoAjusteRequest dto) {
        pedidoService.ajustarPontosCliente(request, id, dto);
        return ResponseEntity.noContent().build();
    }

    private PedidoResumoResponse toResumo(Pedido p) {
        PedidoResumoResponse r = new PedidoResumoResponse();
        r.setId(p.getId());
        r.setClienteId(p.getCliente() != null ? p.getCliente().getId() : null);
        r.setUnidadeId(p.getUnidade() != null ? p.getUnidade().getId() : null);
        r.setCanal(p.getCanal());
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
