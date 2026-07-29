package br.com.cindyperico.raizesdonordeste.dto.produtounidade;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProdutoUnidadeUpsertRequest {

    @NotNull
    private Boolean disponivel;

    @DecimalMin("0.01")
    private BigDecimal precoOverride;

    @Size(max = 120)
    private String nomeOverride;

    @Size(max = 400)
    private String descricaoOverride;

    public Boolean getDisponivel() {
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }

    public BigDecimal getPrecoOverride() {
        return precoOverride;
    }

    public void setPrecoOverride(BigDecimal precoOverride) {
        this.precoOverride = precoOverride;
    }

    public String getNomeOverride() {
        return nomeOverride;
    }

    public void setNomeOverride(String nomeOverride) {
        this.nomeOverride = nomeOverride;
    }

    public String getDescricaoOverride() {
        return descricaoOverride;
    }

    public void setDescricaoOverride(String descricaoOverride) {
        this.descricaoOverride = descricaoOverride;
    }
}
