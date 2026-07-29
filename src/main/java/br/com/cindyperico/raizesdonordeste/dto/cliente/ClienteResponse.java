package br.com.cindyperico.raizesdonordeste.dto.cliente;

import java.time.OffsetDateTime;

public class ClienteResponse {

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Integer pontosFidelidade;
    private Boolean lgpdConsentido;
    private OffsetDateTime lgpdConsentidoEm;
    private Boolean anonimizado;
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
