package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoResponse;
import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Produto;
import br.com.cindyperico.raizesdonordeste.service.ProdutoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> create(HttpServletRequest request, @Valid @RequestBody ProdutoCreateRequest dto) {
        Produto saved = produtoService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> list() {
        return ResponseEntity.ok(produtoService.list().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(produtoService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ProdutoUpdateRequest dto) {
        return ResponseEntity.ok(toResponse(produtoService.update(request, id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        produtoService.delete(request, id);
        return ResponseEntity.noContent().build();
    }

    private ProdutoResponse toResponse(Produto p) {
        ProdutoResponse r = new ProdutoResponse();
        r.setId(p.getId());
        r.setNome(p.getNome());
        r.setDescricao(p.getDescricao());
        r.setPrecoBase(p.getPrecoBase());
        r.setMesInicioSazonal(p.getMesInicioSazonal());
        r.setMesFimSazonal(p.getMesFimSazonal());
        r.setAtivo(p.getAtivo());
        return r;
    }
}
