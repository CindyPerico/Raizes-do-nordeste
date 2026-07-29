package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.cliente.*;
import br.com.cindyperico.raizesdonordeste.model.Cliente;
import br.com.cindyperico.raizesdonordeste.service.ClienteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> create(HttpServletRequest request, @Valid @RequestBody ClienteCreateRequest dto) {
        Cliente saved = clienteService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> list() {
        return ResponseEntity.ok(clienteService.list().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(clienteService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ClienteUpdateRequest dto) {
        return ResponseEntity.ok(toResponse(clienteService.update(request, id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        clienteService.delete(request, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/consentimento")
    public ResponseEntity<ClienteResponse> consentimento(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ClienteConsentRequest dto) {
        return ResponseEntity.ok(toResponse(clienteService.updateConsentimento(request, id, dto)));
    }

    @PostMapping("/{id}/pontos")
    public ResponseEntity<ClienteResponse> addPontos(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ClienteAddPontosRequest dto) {
        return ResponseEntity.ok(toResponse(clienteService.adicionarPontos(request, id, dto)));
    }

    @PostMapping("/{id}/anonimizar")
    public ResponseEntity<ClienteResponse> anonimizar(HttpServletRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(toResponse(clienteService.anonimizar(request, id)));
    }

    private ClienteResponse toResponse(Cliente c) {
        ClienteResponse r = new ClienteResponse();
        r.setId(c.getId());
        r.setNome(c.getNome());
        r.setCpf(c.getCpf());
        r.setEmail(c.getEmail());
        r.setTelefone(c.getTelefone());
        r.setPontosFidelidade(c.getPontosFidelidade());
        r.setLgpdConsentido(c.getLgpdConsentido());
        r.setLgpdConsentidoEm(c.getLgpdConsentidoEm());
        r.setAnonimizado(c.getAnonimizado());
        r.setAnonimizadoEm(c.getAnonimizadoEm());
        return r;
    }
}
