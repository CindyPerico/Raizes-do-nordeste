package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.pedido.*;
import br.com.cindyperico.raizesdonordeste.model.*;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPagamentoExterno;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;
import br.com.cindyperico.raizesdonordeste.model.enums.TipoEventoPedido;
import br.com.cindyperico.raizesdonordeste.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;
    private final PedidoEventoRepository pedidoEventoRepository;

    private final ClienteService clienteService;
    private final UnidadeService unidadeService;
    private final ProdutoService produtoService;
    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final EstoqueItemRepository estoqueItemRepository;
    private final ClienteRepository clienteRepository;

    private final AuditService auditService;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoItemRepository pedidoItemRepository,
                         PedidoEventoRepository pedidoEventoRepository,
                         ClienteService clienteService,
                         UnidadeService unidadeService,
                         ProdutoService produtoService,
                         ProdutoUnidadeRepository produtoUnidadeRepository,
                         EstoqueItemRepository estoqueItemRepository,
                         ClienteRepository clienteRepository,
                         AuditService auditService) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.pedidoEventoRepository = pedidoEventoRepository;
        this.clienteService = clienteService;
        this.unidadeService = unidadeService;
        this.produtoService = produtoService;
        this.produtoUnidadeRepository = produtoUnidadeRepository;
        this.estoqueItemRepository = estoqueItemRepository;
        this.clienteRepository = clienteRepository;
        this.auditService = auditService;
    }

    public Pedido create(HttpServletRequest request, PedidoCreateRequest dto) {
        Unidade unidade = unidadeService.get(dto.getUnidadeId());
        Cliente cliente = null;
        if (dto.getClienteId() != null) {
            cliente = clienteService.get(dto.getClienteId());
        }

        Pedido pedido = new Pedido();
        pedido.setUnidade(unidade);
        pedido.setCliente(cliente);
        pedido.setCanal(dto.getCanal());
        pedido.setStatus(StatusPedido.CRIADO);
        pedido.setStatusPagamentoExterno(StatusPagamentoExterno.NAO_SOLICITADO);
        pedido.setReferenciaPagamentoExterno(null);
        pedido.setCriadoEm(OffsetDateTime.now());
        pedido.setSubtotal(BigDecimal.ZERO);
        pedido.setDesconto(BigDecimal.ZERO);
        pedido.setTotal(BigDecimal.ZERO);

        Pedido savedPedido = pedidoRepository.save(pedido);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PedidoItemCreateRequest itemDto : dto.getItens()) {
            Produto produto = produtoService.get(itemDto.getProdutoId());
            validarProdutoDisponivelNaUnidade(produto.getId(), unidade.getId());
            validarProdutoSazonal(produto);
            validarEstoque(unidade.getId(), produto.getId(), itemDto.getQuantidade());

            BigDecimal preco = resolverPreco(produto, unidade.getId());
            BigDecimal totalItem = preco.multiply(BigDecimal.valueOf(itemDto.getQuantidade()));

            PedidoItem item = new PedidoItem();
            item.setPedido(savedPedido);
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setPrecoUnitario(preco);
            item.setTotalItem(totalItem);
            pedidoItemRepository.save(item);

            subtotal = subtotal.add(totalItem);
        }

        savedPedido.setSubtotal(subtotal);
        savedPedido.setTotal(subtotal.subtract(savedPedido.getDesconto()));
        savedPedido = pedidoRepository.save(savedPedido);

        criarEvento(savedPedido, TipoEventoPedido.STATUS_ALTERADO, "Pedido criado", null, request);
        auditService.log(request, "PEDIDO_CRIADO", "Pedido", savedPedido.getId(), "UnidadeId=" + unidade.getId());

        return savedPedido;
    }

    public List<Pedido> list() {
        return pedidoRepository.findAll();
    }

    public Pedido get(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado"));
    }

    public Pedido updateStatus(HttpServletRequest request, Long id, PedidoStatusUpdateRequest dto) {
        Pedido p = get(id);
        StatusPedido novo = dto.getStatus();

        if (p.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Pedido cancelado não pode mudar de status");
        }

        p.setStatus(novo);
        Pedido saved = pedidoRepository.save(p);

        criarEvento(saved, TipoEventoPedido.STATUS_ALTERADO, "Status alterado para " + novo, null, request);
        auditService.log(request, "PEDIDO_STATUS", "Pedido", id, "Status=" + novo);

        return saved;
    }

    public Pedido solicitarPagamento(HttpServletRequest request, Long id) {
        Pedido p = get(id);

        if (p.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Pedido cancelado");
        }

        p.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        p.setStatusPagamentoExterno(StatusPagamentoExterno.SOLICITADO);

        Pedido saved = pedidoRepository.save(p);
        criarEvento(saved, TipoEventoPedido.CONFIRMACAO_PAGAMENTO, "Pagamento solicitado", null, request);
        auditService.log(request, "PAGAMENTO_SOLICITADO", "Pedido", id, "");

        return saved;
    }

    public Pedido confirmarPagamento(HttpServletRequest request, Long id, PagamentoConfirmacaoRequest dto) {
        Pedido p = get(id);

        if (p.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Pedido cancelado");
        }

        p.setReferenciaPagamentoExterno(dto.getReferenciaExterna());

        if (Boolean.TRUE.equals(dto.getConfirmado())) {
            p.setStatusPagamentoExterno(StatusPagamentoExterno.CONFIRMADO);
            p.setStatus(StatusPedido.PAGO);
            baixarEstoqueDoPedido(p);
            criarEvento(p, TipoEventoPedido.CONFIRMACAO_PAGAMENTO, "Pagamento confirmado", null, request);
            auditService.log(request, "PAGAMENTO_CONFIRMADO", "Pedido", id, "ref=" + dto.getReferenciaExterna());
        } else {
            p.setStatusPagamentoExterno(StatusPagamentoExterno.RECUSADO);
            criarEvento(p, TipoEventoPedido.CONFIRMACAO_PAGAMENTO, "Pagamento recusado", null, request);
            auditService.log(request, "PAGAMENTO_RECUSADO", "Pedido", id, "ref=" + dto.getReferenciaExterna());
        }

        return pedidoRepository.save(p);
    }

    public Pedido aplicarDesconto(HttpServletRequest request, Long id, PedidoDescontoRequest dto) {
        Pedido p = get(id);
        if (p.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("Pedido cancelado");
        }

        if (dto.getValor().compareTo(p.getSubtotal()) > 0) {
            throw new IllegalArgumentException("Desconto não pode ser maior que o subtotal");
        }

        p.setDesconto(dto.getValor());
        p.setTotal(p.getSubtotal().subtract(p.getDesconto()));

        Pedido saved = pedidoRepository.save(p);
        criarEvento(saved, TipoEventoPedido.DESCONTO, dto.getMotivo(), dto.getValor(), request);
        auditService.log(request, "PEDIDO_DESCONTO", "Pedido", id, "valor=" + dto.getValor() + ", motivo=" + dto.getMotivo());

        return saved;
    }

    public Pedido cancelar(HttpServletRequest request, Long id, PedidoCancelarRequest dto) {
        Pedido p = get(id);

        if (p.getStatus() == StatusPedido.CANCELADO) {
            return p;
        }

        if (p.getStatus() == StatusPedido.FINALIZADO) {
            throw new IllegalArgumentException("Pedido finalizado não pode ser cancelado");
        }

        p.setStatus(StatusPedido.CANCELADO);
        Pedido saved = pedidoRepository.save(p);

        criarEvento(saved, TipoEventoPedido.CANCELAMENTO, dto.getMotivo(), null, request);
        auditService.log(request, "PEDIDO_CANCELADO", "Pedido", id, "motivo=" + dto.getMotivo());

        return saved;
    }

    public void ajustarPontosCliente(HttpServletRequest request, Long id, PedidoAjusteRequest dto) {
        Pedido p = get(id);

        if (p.getCliente() == null) {
            throw new IllegalArgumentException("Pedido não possui cliente para ajustar pontos");
        }

        if (dto.getPontosAdicionar() == 0) {
            return;
        }

        Cliente cliente = clienteService.get(p.getCliente().getId());
        cliente.setPontosFidelidade(cliente.getPontosFidelidade() + dto.getPontosAdicionar());
        clienteRepository.save(cliente);

        criarEvento(p, TipoEventoPedido.AJUSTE, dto.getMotivo(), BigDecimal.valueOf(dto.getPontosAdicionar()), request);
        auditService.log(request, "PEDIDO_AJUSTE_PONTOS", "Pedido", id, "pontos=" + dto.getPontosAdicionar() + ", motivo=" + dto.getMotivo());
    }

    private void validarProdutoDisponivelNaUnidade(Long produtoId, Long unidadeId) {
        ProdutoUnidade pu = produtoUnidadeRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não disponível nesta unidade"));

        if (!Boolean.TRUE.equals(pu.getDisponivel())) {
            throw new IllegalArgumentException("Produto indisponível nesta unidade");
        }
    }

    private void validarProdutoSazonal(Produto produto) {
        if (produto.getMesInicioSazonal() == null || produto.getMesFimSazonal() == null) {
            return;
        }

        int mesAtual = OffsetDateTime.now(ZoneId.systemDefault()).getMonthValue();

        int inicio = produto.getMesInicioSazonal();
        int fim = produto.getMesFimSazonal();

        boolean dentro;
        if (inicio <= fim) {
            dentro = mesAtual >= inicio && mesAtual <= fim;
        } else {
            dentro = mesAtual >= inicio || mesAtual <= fim;
        }

        if (!dentro) {
            throw new IllegalArgumentException("Produto sazonal fora do período" );
        }
    }

    private void validarEstoque(Long unidadeId, Long produtoId, int quantidade) {
        EstoqueItem item = estoqueItemRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Sem estoque cadastrado para o produto nesta unidade"));

        if (item.getQuantidade() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }
    }

    private BigDecimal resolverPreco(Produto produto, Long unidadeId) {
        return produtoUnidadeRepository.findByProdutoIdAndUnidadeId(produto.getId(), unidadeId)
                .map(pu -> pu.getPrecoOverride() != null ? pu.getPrecoOverride() : produto.getPrecoBase())
                .orElse(produto.getPrecoBase());
    }

    private void baixarEstoqueDoPedido(Pedido pedido) {
        List<PedidoItem> itens = pedidoItemRepository.findByPedidoId(pedido.getId());

        for (PedidoItem i : itens) {
            EstoqueItem estoque = estoqueItemRepository.findByUnidadeIdAndProdutoId(pedido.getUnidade().getId(), i.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Item de estoque não encontrado"));

            int novaQtd = estoque.getQuantidade() - i.getQuantidade();
            if (novaQtd < 0) {
                throw new IllegalArgumentException("Estoque insuficiente" );
            }
            estoque.setQuantidade(novaQtd);
            estoqueItemRepository.save(estoque);
        }
    }

    private void criarEvento(Pedido pedido, TipoEventoPedido tipo, String motivo, BigDecimal valor, HttpServletRequest request) {
        PedidoEvento ev = new PedidoEvento();
        ev.setPedido(pedido);
        ev.setTipo(tipo);
        ev.setMotivo(motivo);
        ev.setValor(valor);
        ev.setCriadoPor((String) request.getAttribute("audit.username"));
        pedidoEventoRepository.save(ev);
    }
}
