package br.com.cindyperico.raizesdonordeste.dto.produto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProdutoCreateRequest {

    @NotBlank
    @Size(max = 120)
    private String nome;

    @Size(max = 400)
    private String descricao;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal precoBase;

    private Integer mesInicioSazonal;

    private Integer mesFimSazonal;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = precoBase;
    }

    public Integer getMesInicioSazonal() {
        return mesInicioSazonal;
    }

    public void setMesInicioSazonal(Integer mesInicioSazonal) {
        this.mesInicioSazonal = mesInicioSazonal;
    }

    public Integer getMesFimSazonal() {
        return mesFimSazonal;
    }

    public void setMesFimSazonal(Integer mesFimSazonal) {
        this.mesFimSazonal = mesFimSazonal;
    }
}
