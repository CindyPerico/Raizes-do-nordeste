package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.exception.BusinessRuleException;
import br.com.cindyperico.raizesdonordeste.exception.NotFoundException;
import br.com.cindyperico.raizesdonordeste.dto.estoque.EstoqueAjusteRequest;
import br.com.cindyperico.raizesdonordeste.dto.estoque.EstoqueUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.EstoqueItem;
import br.com.cindyperico.raizesdonordeste.model.Produto;
import br.com.cindyperico.raizesdonordeste.model.Unidade;
import br.com.cindyperico.raizesdonordeste.repository.EstoqueItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EstoqueService {

    private final EstoqueItemRepository estoqueItemRepository;
    private final UnidadeService unidadeService;
    private final ProdutoService produtoService;
    private final AuditService auditService;

    public EstoqueService(EstoqueItemRepository estoqueItemRepository,
                          UnidadeService unidadeService,
                          ProdutoService produtoService,
                          AuditService auditService) {
        this.estoqueItemRepository = estoqueItemRepository;
        this.unidadeService = unidadeService;
        this.produtoService = produtoService;
        this.auditService = auditService;
    }

    public EstoqueItem setQuantidade(HttpServletRequest request, Long unidadeId, Long produtoId, EstoqueUpdateRequest dto) {
        Unidade unidade = unidadeService.get(unidadeId);
        Produto produto = produtoService.get(produtoId);

        EstoqueItem item = estoqueItemRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseGet(EstoqueItem::new);

        item.setUnidade(unidade);
        item.setProduto(produto);
        item.setQuantidade(dto.getQuantidade());

        EstoqueItem saved = estoqueItemRepository.save(item);
        auditService.log(request, "ESTOQUE_SET", "EstoqueItem", saved.getId(), "UnidadeId=" + unidadeId + ", ProdutoId=" + produtoId + ", qtd=" + dto.getQuantidade());
        return saved;
    }

    public EstoqueItem ajustar(HttpServletRequest request, Long unidadeId, Long produtoId, EstoqueAjusteRequest dto) {
        Unidade unidade = unidadeService.get(unidadeId);
        Produto produto = produtoService.get(produtoId);

        EstoqueItem item = estoqueItemRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseGet(EstoqueItem::new);

        item.setUnidade(unidade);
        item.setProduto(produto);

        int novaQtd = item.getQuantidade() + dto.getDelta();
        if (novaQtd < 0) {
            throw new BusinessRuleException("Estoque não pode ficar negativo");
        }
        item.setQuantidade(novaQtd);

        EstoqueItem saved = estoqueItemRepository.save(item);
        auditService.log(request, "ESTOQUE_AJUSTE", "EstoqueItem", saved.getId(), "UnidadeId=" + unidadeId + ", ProdutoId=" + produtoId + ", delta=" + dto.getDelta() + ", motivo=" + dto.getMotivo());
        return saved;
    }

    public EstoqueItem getItem(Long unidadeId, Long produtoId) {
        return estoqueItemRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseThrow(() -> new NotFoundException("Item de estoque não encontrado"));
    }
}
