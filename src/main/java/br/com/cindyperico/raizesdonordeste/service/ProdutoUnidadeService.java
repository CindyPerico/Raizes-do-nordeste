package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.produtounidade.CardapioItemResponse;
import br.com.cindyperico.raizesdonordeste.dto.produtounidade.ProdutoUnidadeUpsertRequest;
import br.com.cindyperico.raizesdonordeste.model.Produto;
import br.com.cindyperico.raizesdonordeste.model.ProdutoUnidade;
import br.com.cindyperico.raizesdonordeste.model.Unidade;
import br.com.cindyperico.raizesdonordeste.repository.EstoqueItemRepository;
import br.com.cindyperico.raizesdonordeste.repository.ProdutoUnidadeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProdutoUnidadeService {

    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final EstoqueItemRepository estoqueItemRepository;
    private final ProdutoService produtoService;
    private final UnidadeService unidadeService;
    private final AuditService auditService;

    public ProdutoUnidadeService(ProdutoUnidadeRepository produtoUnidadeRepository,
                                 EstoqueItemRepository estoqueItemRepository,
                                 ProdutoService produtoService,
                                 UnidadeService unidadeService,
                                 AuditService auditService) {
        this.produtoUnidadeRepository = produtoUnidadeRepository;
        this.estoqueItemRepository = estoqueItemRepository;
        this.produtoService = produtoService;
        this.unidadeService = unidadeService;
        this.auditService = auditService;
    }

    public ProdutoUnidade upsert(HttpServletRequest request, Long unidadeId, Long produtoId, ProdutoUnidadeUpsertRequest dto) {
        Produto produto = produtoService.get(produtoId);
        Unidade unidade = unidadeService.get(unidadeId);

        ProdutoUnidade pu = produtoUnidadeRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId)
                .orElseGet(ProdutoUnidade::new);

        pu.setProduto(produto);
        pu.setUnidade(unidade);
        pu.setDisponivel(dto.getDisponivel());
        pu.setPrecoOverride(dto.getPrecoOverride());
        pu.setNomeOverride(dto.getNomeOverride());
        pu.setDescricaoOverride(dto.getDescricaoOverride());

        ProdutoUnidade saved = produtoUnidadeRepository.save(pu);
        auditService.log(request, "PRODUTO_UNIDADE_UPSERT", "ProdutoUnidade", saved.getId(), "ProdutoId=" + produtoId + ", UnidadeId=" + unidadeId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CardapioItemResponse> cardapio(Long unidadeId) {
        unidadeService.get(unidadeId);

        return produtoUnidadeRepository.findByUnidadeIdAndDisponivelTrue(unidadeId).stream()
                .filter(pu -> Boolean.TRUE.equals(pu.getProduto().getAtivo()))
                .map(pu -> toCardapioItem(unidadeId, pu))
                .toList();
    }

    private CardapioItemResponse toCardapioItem(Long unidadeId, ProdutoUnidade pu) {
        Produto produto = pu.getProduto();

        int quantidade = estoqueItemRepository.findByUnidadeIdAndProdutoId(unidadeId, produto.getId())
                .map(item -> item.getQuantidade())
                .orElse(0);

        return new CardapioItemResponse(
                produto.getId(),
                pu.getNomeOverride() != null ? pu.getNomeOverride() : produto.getNome(),
                pu.getDescricaoOverride() != null ? pu.getDescricaoOverride() : produto.getDescricao(),
                pu.getPrecoOverride() != null ? pu.getPrecoOverride() : produto.getPrecoBase(),
                quantidade > 0,
                quantidade);
    }
}
