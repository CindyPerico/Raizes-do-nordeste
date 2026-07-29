package br.com.cindyperico.raizesdonordeste.dto.unidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UnidadeUpdateRequest {

    @NotBlank
    @Size(max = 120)
    private String nome;

    @NotBlank
    @Size(min = 2, max = 2)
    private String uf;

    @NotBlank
    @Size(max = 120)
    private String cidade;

    @Size(max = 200)
    private String endereco;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}
