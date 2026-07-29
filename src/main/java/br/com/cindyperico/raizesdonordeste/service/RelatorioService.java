package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.relatorio.RelatorioFinanceiroResponse;
import br.com.cindyperico.raizesdonordeste.dto.relatorio.RelatorioProdutosMaisVendidosResponse;
import br.com.cindyperico.raizesdonordeste.model.Pedido;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;
import br.com.cindyperico.raizesdonordeste.repository.PedidoRepository;
import br.com.cindyperico.raizesdonordeste.repository.RelatorioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;
    private final PedidoRepository pedidoRepository;
    private final UnidadeService unidadeService;

    public RelatorioService(RelatorioRepository relatorioRepository,
                            PedidoRepository pedidoRepository,
                            UnidadeService unidadeService) {
        this.relatorioRepository = relatorioRepository;
        this.pedidoRepository = pedidoRepository;
        this.unidadeService = unidadeService;
    }

    public List<RelatorioProdutosMaisVendidosResponse> maisVendidos(Long unidadeId) {
        if (unidadeId != null) {
            unidadeService.get(unidadeId);
        }
        return relatorioRepository.produtosMaisVendidos(unidadeId, StatusPedido.CANCELADO);
    }

    public RelatorioFinanceiroResponse financeiro(Long unidadeId) {
        if (unidadeId != null) {
            unidadeService.get(unidadeId);
        }

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                .filter(p -> unidadeId == null || (p.getUnidade() != null && p.getUnidade().getId().equals(unidadeId)))
                .toList();

        long totalPedidos = pedidos.size();
        BigDecimal totalVendido = pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDescontos = pedidos.stream()
                .map(Pedido::getDesconto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioFinanceiroResponse(totalPedidos, totalVendido, totalDescontos);
    }
}
