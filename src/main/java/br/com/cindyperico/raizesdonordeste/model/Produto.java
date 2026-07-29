package br.com.cindyperico.raizesdonordeste.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 400)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precoBase;

    private Integer mesInicioSazonal;

    private Integer mesFimSazonal;

    @Column(nullable = false)
    private Boolean ativo = true;

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
