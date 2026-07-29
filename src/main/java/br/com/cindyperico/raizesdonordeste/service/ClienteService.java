package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.cliente.ClienteAddPontosRequest;
import br.com.cindyperico.raizesdonordeste.dto.cliente.ClienteConsentRequest;
import br.com.cindyperico.raizesdonordeste.dto.cliente.ClienteCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.cliente.ClienteUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Cliente;
import br.com.cindyperico.raizesdonordeste.repository.ClienteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditService auditService;

    public ClienteService(ClienteRepository clienteRepository, AuditService auditService) {
        this.clienteRepository = clienteRepository;
        this.auditService = auditService;
    }

    public Cliente create(HttpServletRequest request, ClienteCreateRequest dto) {
        Cliente c = new Cliente();
        c.setNome(dto.getNome());
        c.setCpf(dto.getCpf());
        c.setEmail(dto.getEmail());
        c.setTelefone(dto.getTelefone());
        c.setPontosFidelidade(0);
        c.setLgpdConsentido(false);
        c.setLgpdConsentidoEm(null);
        c.setAnonimizado(false);
        c.setAnonimizadoEm(null);

        Cliente saved = clienteRepository.save(c);
        auditService.log(request, "CLIENTE_CRIADO", "Cliente", saved.getId(), "Cadastro de cliente");
        return saved;
    }

    public List<Cliente> list() {
        return clienteRepository.findAll();
    }

    public Cliente get(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
    }

    public Cliente update(HttpServletRequest request, Long id, ClienteUpdateRequest dto) {
        Cliente c = get(id);
        c.setNome(dto.getNome());
        c.setCpf(dto.getCpf());
        c.setEmail(dto.getEmail());
        c.setTelefone(dto.getTelefone());

        Cliente saved = clienteRepository.save(c);
        auditService.log(request, "CLIENTE_ATUALIZADO", "Cliente", saved.getId(), "Atualização de dados" );
        return saved;
    }

    public void delete(HttpServletRequest request, Long id) {
        Cliente c = get(id);
        clienteRepository.delete(c);
        auditService.log(request, "CLIENTE_REMOVIDO", "Cliente", id, "Remoção" );
    }

    public Cliente updateConsentimento(HttpServletRequest request, Long id, ClienteConsentRequest dto) {
        Cliente c = get(id);

        if (Boolean.TRUE.equals(dto.getConsentido())) {
            c.setLgpdConsentido(true);
            c.setLgpdConsentidoEm(OffsetDateTime.now());
            Cliente saved = clienteRepository.save(c);
            auditService.log(request, "CLIENTE_CONSENTIMENTO_LGPD", "Cliente", id, "Consentimento: true");
            return saved;
        }

        c.setLgpdConsentido(false);
        c.setLgpdConsentidoEm(null);
        Cliente saved = clienteRepository.save(c);
        auditService.log(request, "CLIENTE_CONSENTIMENTO_LGPD", "Cliente", id, "Consentimento: false");
        return saved;
    }

    public Cliente adicionarPontos(HttpServletRequest request, Long id, ClienteAddPontosRequest dto) {
        Cliente c = get(id);
        c.setPontosFidelidade(c.getPontosFidelidade() + dto.getPontos());
        Cliente saved = clienteRepository.save(c);
        auditService.log(request, "CLIENTE_PONTOS_ADICIONADOS", "Cliente", id, "Pontos: +" + dto.getPontos());
        return saved;
    }

    public Cliente anonimizar(HttpServletRequest request, Long id) {
        Cliente c = get(id);

        c.setNome("ANONIMIZADO");
        c.setCpf(null);
        c.setEmail(null);
        c.setTelefone(null);
        c.setAnonimizado(true);
        c.setAnonimizadoEm(OffsetDateTime.now());

        Cliente saved = clienteRepository.save(c);
        auditService.log(request, "CLIENTE_ANONIMIZADO", "Cliente", id, "Anonimização LGPD" );
        return saved;
    }
}
