package br.com.cindyperico.raizesdonordeste.dto.produtounidade;

import java.math.BigDecimal;

public record CardapioItemResponse(
        Long produtoId,
        String nome,
        String descricao,
        BigDecimal preco,
        boolean disponivel,
        int quantidadeEmEstoque) {
}
