package br.com.cindyperico.raizesdonordeste.model;

import br.com.cindyperico.raizesdonordeste.model.enums.CanalAtendimento;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPagamentoExterno;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_pedido", nullable = false, length = 20)
    private CanalAtendimento canalPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status = StatusPedido.CRIADO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPagamentoExterno statusPagamentoExterno = StatusPagamentoExterno.NAO_SOLICITADO;

    @Column(length = 80)
    private String referenciaPagamentoExterno;

    @Column(nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
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
