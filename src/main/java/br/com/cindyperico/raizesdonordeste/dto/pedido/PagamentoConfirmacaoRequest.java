package br.com.cindyperico.raizesdonordeste.dto.pedido;

import jakarta.validation.constraints.NotNull;

public class PagamentoConfirmacaoRequest {

    @NotNull
    private Boolean confirmado;

    private String referenciaExterna;

    public Boolean getConfirmado() {
        return confirmado;
    }

    public void setConfirmado(Boolean confirmado) {
        this.confirmado = confirmado;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }
}
