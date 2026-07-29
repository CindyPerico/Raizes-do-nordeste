package br.com.cindyperico.raizesdonordeste.dto.produto;

import java.math.BigDecimal;

public class ProdutoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private Integer mesInicioSazonal;
    private Integer mesFimSazonal;
    private Boolean ativo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
