package br.com.cindyperico.raizesdonordeste.repository;

import br.com.cindyperico.raizesdonordeste.dto.relatorio.RelatorioProdutosMaisVendidosResponse;
import br.com.cindyperico.raizesdonordeste.model.Pedido;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelatorioRepository extends JpaRepository<Pedido, Long> {

    @Query("select new br.com.cindyperico.raizesdonordeste.dto.relatorio.RelatorioProdutosMaisVendidosResponse(i.produto.id, i.produto.nome, sum(i.quantidade)) " +
            "from PedidoItem i " +
            "where i.pedido.status <> :cancelado " +
            "and (:unidadeId is null or i.pedido.unidade.id = :unidadeId) " +
            "group by i.produto.id, i.produto.nome " +
            "order by sum(i.quantidade) desc")
    List<RelatorioProdutosMaisVendidosResponse> produtosMaisVendidos(@Param("unidadeId") Long unidadeId,
                                                                    @Param("cancelado") StatusPedido cancelado);
}
