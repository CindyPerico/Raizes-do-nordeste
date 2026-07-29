package br.com.cindyperico.raizesdonordeste.dto.pedido;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PedidoDescontoRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valor;

    @NotBlank
    private String motivo;

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
