package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.PaginaResponse;
import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoResponse;
import br.com.cindyperico.raizesdonordeste.dto.produto.ProdutoUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Produto;
import br.com.cindyperico.raizesdonordeste.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Catálogo de produtos da rede")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um produto no catálogo da rede")
    public ResponseEntity<ProdutoResponse> create(HttpServletRequest request, @Valid @RequestBody ProdutoCreateRequest dto) {
        Produto saved = produtoService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    @Operation(summary = "Lista os produtos do catálogo de forma paginada")
    public ResponseEntity<PaginaResponse<ProdutoResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id"));
        return ResponseEntity.ok(PaginaResponse.of(produtoService.list(pageable), this::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um produto pelo identificador")
    public ResponseEntity<ProdutoResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(produtoService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto do catálogo")
    public ResponseEntity<ProdutoResponse> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ProdutoUpdateRequest dto) {
        return ResponseEntity.ok(toResponse(produtoService.update(request, id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produto do catálogo")
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
