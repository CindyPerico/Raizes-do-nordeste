package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.relatorio.RelatorioFinanceiroResponse;
import br.com.cindyperico.raizesdonordeste.dto.relatorio.RelatorioProdutosMaisVendidosResponse;
import br.com.cindyperico.raizesdonordeste.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/mais-vendidos")
    public ResponseEntity<List<RelatorioProdutosMaisVendidosResponse>> maisVendidos(@RequestParam(required = false) Long unidadeId) {
        return ResponseEntity.ok(relatorioService.maisVendidos(unidadeId));
    }

    @GetMapping("/financeiro")
    public ResponseEntity<RelatorioFinanceiroResponse> financeiro(@RequestParam(required = false) Long unidadeId) {
        return ResponseEntity.ok(relatorioService.financeiro(unidadeId));
    }
}
