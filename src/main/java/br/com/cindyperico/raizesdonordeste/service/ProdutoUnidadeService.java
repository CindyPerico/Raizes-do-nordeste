package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.produtounidade.ProdutoUnidadeUpsertRequest;
import br.com.cindyperico.raizesdonordeste.model.Produto;
import br.com.cindyperico.raizesdonordeste.model.ProdutoUnidade;
import br.com.cindyperico.raizesdonordeste.model.Unidade;
import br.com.cindyperico.raizesdonordeste.repository.ProdutoUnidadeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ProdutoUnidadeService {

    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final ProdutoService produtoService;
    private final UnidadeService unidadeService;
    private final AuditService auditService;

    public ProdutoUnidadeService(ProdutoUnidadeRepository produtoUnidadeRepository,
                                 ProdutoService produtoService,
                                 UnidadeService unidadeService,
                                 AuditService auditService) {
        this.produtoUnidadeRepository = produtoUnidadeRepository;
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
}
