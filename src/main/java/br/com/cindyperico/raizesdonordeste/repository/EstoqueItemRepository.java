package br.com.cindyperico.raizesdonordeste.repository;

import br.com.cindyperico.raizesdonordeste.model.EstoqueItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstoqueItemRepository extends JpaRepository<EstoqueItem, Long> {
    Optional<EstoqueItem> findByUnidadeIdAndProdutoId(Long unidadeId, Long produtoId);
}
