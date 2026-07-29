package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.unidade.UnidadeCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.unidade.UnidadeResponse;
import br.com.cindyperico.raizesdonordeste.dto.unidade.UnidadeUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Unidade;
import br.com.cindyperico.raizesdonordeste.service.UnidadeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @PostMapping
    public ResponseEntity<UnidadeResponse> create(HttpServletRequest request, @Valid @RequestBody UnidadeCreateRequest dto) {
        Unidade saved = unidadeService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<UnidadeResponse>> list() {
        return ResponseEntity.ok(unidadeService.list().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(unidadeService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadeResponse> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody UnidadeUpdateRequest dto) {
        return ResponseEntity.ok(toResponse(unidadeService.update(request, id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        unidadeService.delete(request, id);
        return ResponseEntity.noContent().build();
    }

    private UnidadeResponse toResponse(Unidade u) {
        UnidadeResponse r = new UnidadeResponse();
        r.setId(u.getId());
        r.setNome(u.getNome());
        r.setUf(u.getUf());
        r.setCidade(u.getCidade());
        r.setEndereco(u.getEndereco());
        return r;
    }
}
