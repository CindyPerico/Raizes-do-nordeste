package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.produtounidade.ProdutoUnidadeUpsertRequest;
import br.com.cindyperico.raizesdonordeste.model.ProdutoUnidade;
import br.com.cindyperico.raizesdonordeste.service.ProdutoUnidadeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unidades/{unidadeId}/produtos")
public class UnidadeProdutoController {

    private final ProdutoUnidadeService produtoUnidadeService;

    public UnidadeProdutoController(ProdutoUnidadeService produtoUnidadeService) {
        this.produtoUnidadeService = produtoUnidadeService;
    }

    @PutMapping("/{produtoId}")
    public ResponseEntity<ProdutoUnidade> upsert(HttpServletRequest request,
                                                @PathVariable Long unidadeId,
                                                @PathVariable Long produtoId,
                                                @Valid @RequestBody ProdutoUnidadeUpsertRequest dto) {
        return ResponseEntity.ok(produtoUnidadeService.upsert(request, unidadeId, produtoId, dto));
    }
}
