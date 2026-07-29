package br.com.cindyperico.raizesdonordeste.dto.estoque;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EstoqueUpdateRequest {

    @NotNull
    @Min(0)
    private Integer quantidade;

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
