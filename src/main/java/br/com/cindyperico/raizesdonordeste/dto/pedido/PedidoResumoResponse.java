package br.com.cindyperico.raizesdonordeste.dto.pedido;

import br.com.cindyperico.raizesdonordeste.model.enums.CanalAtendimento;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPagamentoExterno;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PedidoResumoResponse {

    private Long id;
    private Long clienteId;
    private Long unidadeId;
    private CanalAtendimento canalPedido;
    private StatusPedido status;
    private StatusPagamentoExterno statusPagamentoExterno;
    private String referenciaPagamentoExterno;
    private OffsetDateTime criadoEm;
    private BigDecimal subtotal;
    private BigDecimal desconto;
    private BigDecimal total;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
    }

    public CanalAtendimento getCanalPedido() {
        return canalPedido;
    }

    public void setCanalPedido(CanalAtendimento canalPedido) {
        this.canalPedido = canalPedido;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public StatusPagamentoExterno getStatusPagamentoExterno() {
        return statusPagamentoExterno;
    }

    public void setStatusPagamentoExterno(StatusPagamentoExterno statusPagamentoExterno) {
        this.statusPagamentoExterno = statusPagamentoExterno;
    }

    public String getReferenciaPagamentoExterno() {
        return referenciaPagamentoExterno;
    }

    public void setReferenciaPagamentoExterno(String referenciaPagamentoExterno) {
        this.referenciaPagamentoExterno = referenciaPagamentoExterno;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
