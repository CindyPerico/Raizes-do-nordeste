package br.com.cindyperico.raizesdonordeste.dto.pedido;

import jakarta.validation.constraints.NotBlank;

public class PedidoCancelarRequest {

    @NotBlank
    private String motivo;

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
