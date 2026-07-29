package br.com.cindyperico.raizesdonordeste.dto.relatorio;

import java.math.BigDecimal;

public class RelatorioFinanceiroResponse {

    private Long totalPedidos;
    private BigDecimal totalVendido;
    private BigDecimal totalDescontos;

    public RelatorioFinanceiroResponse(Long totalPedidos, BigDecimal totalVendido, BigDecimal totalDescontos) {
        this.totalPedidos = totalPedidos;
        this.totalVendido = totalVendido;
        this.totalDescontos = totalDescontos;
    }

    public Long getTotalPedidos() {
        return totalPedidos;
    }

    public BigDecimal getTotalVendido() {
        return totalVendido;
    }

    public BigDecimal getTotalDescontos() {
        return totalDescontos;
    }
}
