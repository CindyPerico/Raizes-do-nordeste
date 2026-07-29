package br.com.cindyperico.raizesdonordeste.dto.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PedidoAjusteRequest {

    @NotNull
    private Integer pontosAdicionar;

    @NotBlank
    private String motivo;

    public Integer getPontosAdicionar() {
        return pontosAdicionar;
    }

    public void setPontosAdicionar(Integer pontosAdicionar) {
        this.pontosAdicionar = pontosAdicionar;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
