package br.com.cindyperico.raizesdonordeste.dto.relatorio;

public class RelatorioProdutosMaisVendidosResponse {

    private Long produtoId;
    private String produtoNome;
    private Long quantidadeVendida;

    public RelatorioProdutosMaisVendidosResponse(Long produtoId, String produtoNome, Long quantidadeVendida) {
        this.produtoId = produtoId;
        this.produtoNome = produtoNome;
        this.quantidadeVendida = quantidadeVendida;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public Long getQuantidadeVendida() {
        return quantidadeVendida;
    }
}
