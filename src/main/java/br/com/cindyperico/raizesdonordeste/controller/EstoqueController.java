package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.estoque.EstoqueAjusteRequest;
import br.com.cindyperico.raizesdonordeste.dto.estoque.EstoqueUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.EstoqueItem;
import br.com.cindyperico.raizesdonordeste.service.EstoqueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unidades/{unidadeId}/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping("/produtos/{produtoId}")
    public ResponseEntity<EstoqueItem> get(@PathVariable Long unidadeId, @PathVariable Long produtoId) {
        return ResponseEntity.ok(estoqueService.getItem(unidadeId, produtoId));
    }

    @PutMapping("/produtos/{produtoId}")
    public ResponseEntity<EstoqueItem> setQuantidade(HttpServletRequest request,
                                                     @PathVariable Long unidadeId,
                                                     @PathVariable Long produtoId,
                                                     @Valid @RequestBody EstoqueUpdateRequest dto) {
        return ResponseEntity.ok(estoqueService.setQuantidade(request, unidadeId, produtoId, dto));
    }

    @PostMapping("/produtos/{produtoId}/ajuste")
    public ResponseEntity<EstoqueItem> ajuste(HttpServletRequest request,
                                             @PathVariable Long unidadeId,
                                             @PathVariable Long produtoId,
                                             @Valid @RequestBody EstoqueAjusteRequest dto) {
        return ResponseEntity.ok(estoqueService.ajustar(request, unidadeId, produtoId, dto));
    }
}
