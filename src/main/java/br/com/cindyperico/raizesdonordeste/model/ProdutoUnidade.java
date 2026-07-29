package br.com.cindyperico.raizesdonordeste.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos_unidades",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_produto_unidade", columnNames = {"produto_id", "unidade_id"})
        }
)
public class ProdutoUnidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @Column(nullable = false)
    private Boolean disponivel = true;

    @Column(precision = 12, scale = 2)
    private BigDecimal precoOverride;

    @Column(length = 120)
    private String nomeOverride;

    @Column(length = 400)
    private String descricaoOverride;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

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
