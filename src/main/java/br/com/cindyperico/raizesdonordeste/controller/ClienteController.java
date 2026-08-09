package br.com.cindyperico.raizesdonordeste.controller;

import br.com.cindyperico.raizesdonordeste.dto.PaginaResponse;
import br.com.cindyperico.raizesdonordeste.dto.cliente.*;
import br.com.cindyperico.raizesdonordeste.model.Cliente;
import br.com.cindyperico.raizesdonordeste.service.ClienteService;
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
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Cadastro de clientes, consentimento LGPD e fidelidade")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um cliente")
    public ResponseEntity<ClienteResponse> create(HttpServletRequest request, @Valid @RequestBody ClienteCreateRequest dto) {
        Cliente saved = clienteService.create(request, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    @Operation(summary = "Lista clientes de forma paginada")
    public ResponseEntity<PaginaResponse<ClienteResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id"));
        return ResponseEntity.ok(PaginaResponse.of(clienteService.list(pageable), this::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um cliente pelo identificador")
    public ResponseEntity<ClienteResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(clienteService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados cadastrais do cliente")
    public ResponseEntity<ClienteResponse> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ClienteUpdateRequest dto) {
        return ResponseEntity.ok(toResponse(clienteService.update(request, id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um cliente (restrito ao perfil ADMIN)")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        clienteService.delete(request, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/consentimento")
    @Operation(summary = "Registra o consentimento LGPD do cliente")
    public ResponseEntity<ClienteResponse> consentimento(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ClienteConsentRequest dto) {
        return ResponseEntity.ok(toResponse(clienteService.updateConsentimento(request, id, dto)));
    }

    @PostMapping("/{id}/pontos")
    @Operation(summary = "Credita pontos de fidelidade (exige consentimento LGPD)")
    public ResponseEntity<ClienteResponse> addPontos(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ClienteAddPontosRequest dto) {
        return ResponseEntity.ok(toResponse(clienteService.adicionarPontos(request, id, dto)));
    }

    @PostMapping("/{id}/pontos/resgatar")
    @Operation(summary = "Resgata pontos de fidelidade (exige consentimento e saldo suficiente)")
    public ResponseEntity<ClienteResponse> resgatarPontos(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ClienteResgatePontosRequest dto) {
        return ResponseEntity.ok(toResponse(clienteService.resgatarPontos(request, id, dto)));
    }

    @PostMapping("/{id}/anonimizar")
    @Operation(summary = "Anonimiza os dados pessoais do cliente (direito de eliminação — LGPD)")
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
