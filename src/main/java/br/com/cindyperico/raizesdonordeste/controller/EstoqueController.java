package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.estoque.EstoqueAjusteRequest;
import br.com.cindyperico.raizesdonordeste.dto.estoque.EstoqueResponse;
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
    public ResponseEntity<EstoqueResponse> get(
            @PathVariable Long unidadeId,
            @PathVariable Long produtoId) {

        return ResponseEntity.ok(toResponse(
                estoqueService.getItem(unidadeId, produtoId)
        ));
    }

    @PutMapping("/produtos/{produtoId}")
    public ResponseEntity<EstoqueResponse> setQuantidade(
            HttpServletRequest request,
            @PathVariable Long unidadeId,
            @PathVariable Long produtoId,
            @Valid @RequestBody EstoqueUpdateRequest dto) {

        return ResponseEntity.ok(toResponse(
                estoqueService.setQuantidade(request, unidadeId, produtoId, dto)
        ));
    }

    @PostMapping("/produtos/{produtoId}/ajuste")
    public ResponseEntity<EstoqueResponse> ajuste(
            HttpServletRequest request,
            @PathVariable Long unidadeId,
            @PathVariable Long produtoId,
            @Valid @RequestBody EstoqueAjusteRequest dto) {

        return ResponseEntity.ok(toResponse(
                estoqueService.ajustar(request, unidadeId, produtoId, dto)
        ));
    }

    private EstoqueResponse toResponse(EstoqueItem item) {
        EstoqueResponse response = new EstoqueResponse();

        response.setId(item.getId());
        response.setUnidadeId(item.getUnidade().getId());
        response.setProdutoId(item.getProduto().getId());
        response.setQuantidade(item.getQuantidade());

        return response;
    }
}
