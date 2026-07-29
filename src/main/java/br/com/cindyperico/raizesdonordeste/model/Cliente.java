package br.com.cindyperico.raizesdonordeste.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 14)
    private String cpf;

    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false)
    private Integer pontosFidelidade = 0;

    @Column(nullable = false)
    private Boolean lgpdConsentido = false;

    private OffsetDateTime lgpdConsentidoEm;

    @Column(nullable = false)
    private Boolean anonimizado = false;

    private OffsetDateTime anonimizadoEm;

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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Integer getPontosFidelidade() {
        return pontosFidelidade;
    }

    public void setPontosFidelidade(Integer pontosFidelidade) {
        this.pontosFidelidade = pontosFidelidade;
    }

    public Boolean getLgpdConsentido() {
        return lgpdConsentido;
    }

    public void setLgpdConsentido(Boolean lgpdConsentido) {
        this.lgpdConsentido = lgpdConsentido;
    }

    public OffsetDateTime getLgpdConsentidoEm() {
        return lgpdConsentidoEm;
    }

    public void setLgpdConsentidoEm(OffsetDateTime lgpdConsentidoEm) {
        this.lgpdConsentidoEm = lgpdConsentidoEm;
    }

    public Boolean getAnonimizado() {
        return anonimizado;
    }

    public void setAnonimizado(Boolean anonimizado) {
        this.anonimizado = anonimizado;
    }

    public OffsetDateTime getAnonimizadoEm() {
        return anonimizadoEm;
    }

    public void setAnonimizadoEm(OffsetDateTime anonimizadoEm) {
        this.anonimizadoEm = anonimizadoEm;
    }
}
