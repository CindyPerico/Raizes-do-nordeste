package br.com.cindyperico.raizesdonordeste.dto.cliente;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ClienteAddPontosRequest {

    @NotNull
    @Min(1)
    private Integer pontos;

    public Integer getPontos() {
        return pontos;
    }

    public void setPontos(Integer pontos) {
        this.pontos = pontos;
    }
}
