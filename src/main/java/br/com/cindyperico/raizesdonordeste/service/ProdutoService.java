package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Produto;
import br.com.cindyperico.raizesdonordeste.repository.ProdutoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final AuditService auditService;

    public ProdutoService(ProdutoRepository produtoRepository, AuditService auditService) {
        this.produtoRepository = produtoRepository;
        this.auditService = auditService;
    }

    public Produto create(HttpServletRequest request, ProdutoCreateRequest dto) {
        validarSazonalidade(dto.getMesInicioSazonal(), dto.getMesFimSazonal());

        Produto p = new Produto();
        p.setNome(dto.getNome());
        p.setDescricao(dto.getDescricao());
        p.setPrecoBase(dto.getPrecoBase());
        p.setMesInicioSazonal(dto.getMesInicioSazonal());
        p.setMesFimSazonal(dto.getMesFimSazonal());
        p.setAtivo(true);

        Produto saved = produtoRepository.save(p);
        auditService.log(request, "PRODUTO_CRIADO", "Produto", saved.getId(), "Cadastro de produto" );
        return saved;
    }

    public List<Produto> list() {
        return produtoRepository.findAll();
    }

    public Produto get(Long id) {
        return produtoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
    }

    public Produto update(HttpServletRequest request, Long id, ProdutoUpdateRequest dto) {
        validarSazonalidade(dto.getMesInicioSazonal(), dto.getMesFimSazonal());

        Produto p = get(id);
        p.setNome(dto.getNome());
        p.setDescricao(dto.getDescricao());
        p.setPrecoBase(dto.getPrecoBase());
        p.setMesInicioSazonal(dto.getMesInicioSazonal());
        p.setMesFimSazonal(dto.getMesFimSazonal());
        if (dto.getAtivo() != null) {
            p.setAtivo(dto.getAtivo());
        }

        Produto saved = produtoRepository.save(p);
        auditService.log(request, "PRODUTO_ATUALIZADO", "Produto", id, "Atualização" );
        return saved;
    }

    public void delete(HttpServletRequest request, Long id) {
        Produto p = get(id);
        produtoRepository.delete(p);
        auditService.log(request, "PRODUTO_REMOVIDO", "Produto", id, "Remoção" );
    }

    private void validarSazonalidade(Integer inicio, Integer fim) {
        if (inicio == null && fim == null) {
            return;
        }
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Sazonalidade inválida: informe mês início e mês fim");
        }
        if (inicio < 1 || inicio > 12 || fim < 1 || fim > 12) {
            throw new IllegalArgumentException("Sazonalidade inválida: meses devem estar entre 1 e 12");
        }
    }
}
