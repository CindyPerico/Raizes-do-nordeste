package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.produtounidade.CardapioItemResponse;
import br.com.cindyperico.raizesdonordeste.dto.produtounidade.ProdutoUnidadeUpsertRequest;
import br.com.cindyperico.raizesdonordeste.model.ProdutoUnidade;
import br.com.cindyperico.raizesdonordeste.service.ProdutoUnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades/{unidadeId}/produtos")
@Tag(name = "Cardápio por unidade", description = "Cardápio disponível em cada unidade da rede")
public class UnidadeProdutoController {

    private final ProdutoUnidadeService produtoUnidadeService;

    public UnidadeProdutoController(ProdutoUnidadeService produtoUnidadeService) {
        this.produtoUnidadeService = produtoUnidadeService;
    }

    @GetMapping
    @Operation(summary = "Consulta o cardápio disponível da unidade, com preço praticado e saldo de estoque")
    public ResponseEntity<List<CardapioItemResponse>> cardapio(@PathVariable Long unidadeId) {
        return ResponseEntity.ok(produtoUnidadeService.cardapio(unidadeId));
    }

    @PutMapping("/{produtoId}")
    @Operation(summary = "Disponibiliza ou atualiza um produto no cardápio da unidade")
    public ResponseEntity<ProdutoUnidade> upsert(HttpServletRequest request,
                                                @PathVariable Long unidadeId,
                                                @PathVariable Long produtoId,
                                                @Valid @RequestBody ProdutoUnidadeUpsertRequest dto) {
        return ResponseEntity.ok(produtoUnidadeService.upsert(request, unidadeId, produtoId, dto));
    }
}
