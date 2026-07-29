package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.funcionario.FuncionarioCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.funcionario.FuncionarioResponse;
import br.com.cindyperico.raizesdonordeste.dto.funcionario.FuncionarioUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Funcionario;
import br.com.cindyperico.raizesdonordeste.service.FuncionarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponse> create(HttpServletRequest request, @Valid @RequestBody FuncionarioCreateRequest dto) {
        Funcionario saved = funcionarioService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioResponse>> list(@RequestParam(required = false) Long unidadeId) {
        List<Funcionario> list = unidadeId != null
                ? funcionarioService.listByUnidade(unidadeId)
                : funcionarioService.list();

        return ResponseEntity.ok(list.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(funcionarioService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioResponse> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody FuncionarioUpdateRequest dto) {
        return ResponseEntity.ok(toResponse(funcionarioService.update(request, id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        funcionarioService.delete(request, id);
        return ResponseEntity.noContent().build();
    }

    private FuncionarioResponse toResponse(Funcionario f) {
        FuncionarioResponse r = new FuncionarioResponse();
        r.setId(f.getId());
        r.setNome(f.getNome());
        r.setCargo(f.getCargo());
        r.setAtivo(f.getAtivo());
        r.setUnidadeId(f.getUnidade() != null ? f.getUnidade().getId() : null);
        return r;
    }
}
