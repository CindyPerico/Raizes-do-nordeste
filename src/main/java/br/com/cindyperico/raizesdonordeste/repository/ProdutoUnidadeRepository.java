package br.com.cindyperico.raizesdonordeste.repository;

import br.com.cindyperico.raizesdonordeste.model.ProdutoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoUnidadeRepository extends JpaRepository<ProdutoUnidade, Long> {

    Optional<ProdutoUnidade> findByProdutoIdAndUnidadeId(Long produtoId, Long unidadeId);

    List<ProdutoUnidade> findByUnidadeIdAndDisponivelTrue(Long unidadeId);
}
