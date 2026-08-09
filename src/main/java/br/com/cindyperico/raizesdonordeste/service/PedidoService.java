package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.pedido.*;
import br.com.cindyperico.raizesdonordeste.exception.BusinessRuleException;
import br.com.cindyperico.raizesdonordeste.exception.NotFoundException;
import br.com.cindyperico.raizesdonordeste.model.*;
import br.com.cindyperico.raizesdonordeste.model.enums.CanalAtendimento;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPagamentoExterno;
import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;
import br.com.cindyperico.raizesdonordeste.model.enums.TipoEventoPedido;
import br.com.cindyperico.raizesdonordeste.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PedidoService {

    private static final BigDecimal REAIS_POR_PONTO = BigDecimal.TEN;

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
        pedido.setCanalPedido(dto.getCanalPedido());
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
        auditService.log(request, "PEDIDO_CRIADO", "Pedido", savedPedido.getId(), "UnidadeId=" + unidade.getId()
                + ", canalPedido=" + savedPedido.getCanalPedido());

        return savedPedido;
    }

    public Page<Pedido> buscar(CanalAtendimento canalPedido, StatusPedido status, Long unidadeId, Pageable pageable) {
        Specification<Pedido> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (canalPedido != null) {
                predicates.add(cb.equal(root.get("canalPedido"), canalPedido));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (unidadeId != null) {
                predicates.add(cb.equal(root.get("unidade").get("id"), unidadeId));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return pedidoRepository.findAll(spec, pageable);
    }

    public Pedido get(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
    }

    public List<PedidoItem> itensDoPedido(Long pedidoId) {
        return pedidoItemRepository.findByPedidoId(pedidoId);
    }

    public Pedido updateStatus(HttpServletRequest request, Long id, PedidoStatusUpdateRequest dto) {
        Pedido p = get(id);
        StatusPedido novo = dto.getStatus();

        if (p.getStatus() == StatusPedido.CANCELADO) {
            throw new BusinessRuleException("Pedido cancelado não pode mudar de status");
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
            throw new BusinessRuleException("Pedido cancelado");
        }

        if (p.getStatusPagamentoExterno() == StatusPagamentoExterno.CONFIRMADO) {
            throw new BusinessRuleException("Pagamento já confirmado para este pedido");
        }

        p.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        p.setStatusPagamentoExterno(StatusPagamentoExterno.SOLICITADO);
        p.setReferenciaPagamentoExterno("MOCK-" + UUID.randomUUID());

        Pedido saved = pedidoRepository.save(p);
        criarEvento(saved, TipoEventoPedido.CONFIRMACAO_PAGAMENTO, "Pagamento solicitado ao gateway mock", null, request);
        auditService.log(request, "PAGAMENTO_SOLICITADO", "Pedido", id, "ref=" + saved.getReferenciaPagamentoExterno());

        return saved;
    }

    public Pedido confirmarPagamento(HttpServletRequest request, Long id, PagamentoConfirmacaoRequest dto) {
        Pedido p = get(id);

        if (p.getStatus() == StatusPedido.CANCELADO) {
            throw new BusinessRuleException("Pedido cancelado");
        }

        if (p.getStatusPagamentoExterno() == StatusPagamentoExterno.NAO_SOLICITADO) {
            throw new BusinessRuleException("Pagamento ainda não foi solicitado para este pedido");
        }

        if (dto.getReferenciaExterna() != null) {
            p.setReferenciaPagamentoExterno(dto.getReferenciaExterna());
        }

        if (Boolean.TRUE.equals(dto.getConfirmado())) {
            p.setStatusPagamentoExterno(StatusPagamentoExterno.CONFIRMADO);
            p.setStatus(StatusPedido.PAGO);
            baixarEstoqueDoPedido(p);
            creditarPontosFidelidade(request, p);
            criarEvento(p, TipoEventoPedido.CONFIRMACAO_PAGAMENTO, "Pagamento confirmado", null, request);
            auditService.log(request, "PAGAMENTO_CONFIRMADO", "Pedido", id, "ref=" + p.getReferenciaPagamentoExterno());
        } else {
            p.setStatusPagamentoExterno(StatusPagamentoExterno.RECUSADO);
            criarEvento(p, TipoEventoPedido.CONFIRMACAO_PAGAMENTO, "Pagamento recusado pelo gateway mock", null, request);
            auditService.log(request, "PAGAMENTO_RECUSADO", "Pedido", id, "ref=" + p.getReferenciaPagamentoExterno());
        }

        return pedidoRepository.save(p);
    }

    public Pedido aplicarDesconto(HttpServletRequest request, Long id, PedidoDescontoRequest dto) {
        Pedido p = get(id);
        if (p.getStatus() == StatusPedido.CANCELADO) {
            throw new BusinessRuleException("Pedido cancelado");
        }

        if (dto.getValor().compareTo(p.getSubtotal()) > 0) {
            throw new BusinessRuleException("Desconto não pode ser maior que o subtotal");
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
            throw new BusinessRuleException("Pedido finalizado não pode ser cancelado");
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
            throw new BusinessRuleException("Pedido não possui cliente para ajustar pontos");
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

    private void creditarPontosFidelidade(HttpServletRequest request, Pedido pedido) {
        Cliente cliente = pedido.getCliente();

        if (cliente == null || !Boolean.TRUE.equals(cliente.getLgpdConsentido())) {
            return;
        }

        int pontos = pedido.getTotal().divideToIntegralValue(REAIS_POR_PONTO).intValue();
        if (pontos <= 0) {
            return;
        }

        cliente.setPontosFidelidade(cliente.getPontosFidelidade() + pontos);
        clienteRepository.save(cliente);
        auditService.log(request, "FIDELIDADE_PONTOS_CREDITADOS", "Cliente", cliente.getId(),
                "pedidoId=" + pedido.getId() + ", pontos=" + pontos);
    }

    private void validarProdutoDisponivelNaUnidade(Long produtoId, Long unidadeId) {
        ProdutoUnidade pu = produtoUnidadeRepository.findByProdutoIdAndUnidadeId(produtoId, unidadeId)
                .orElseThrow(() -> new NotFoundException("Produto não faz parte do cardápio desta unidade"));

        if (!Boolean.TRUE.equals(pu.getDisponivel())) {
            throw new BusinessRuleException("Produto indisponível nesta unidade");
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
            throw new BusinessRuleException("Produto sazonal fora do período");
        }
    }

    private void validarEstoque(Long unidadeId, Long produtoId, int quantidade) {
        EstoqueItem item = estoqueItemRepository.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseThrow(() -> new BusinessRuleException("Sem estoque cadastrado para o produto nesta unidade"));

        if (item.getQuantidade() < quantidade) {
            throw new BusinessRuleException("Estoque insuficiente");
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
                    .orElseThrow(() -> new BusinessRuleException("Item de estoque não encontrado"));

            int novaQtd = estoque.getQuantidade() - i.getQuantidade();
            if (novaQtd < 0) {
                throw new BusinessRuleException("Estoque insuficiente");
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
