package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.exception.NotFoundException;
import br.com.cindyperico.raizesdonordeste.dto.unidade.UnidadeCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.unidade.UnidadeUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Unidade;
import br.com.cindyperico.raizesdonordeste.repository.UnidadeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;
    private final AuditService auditService;

    public UnidadeService(UnidadeRepository unidadeRepository, AuditService auditService) {
        this.unidadeRepository = unidadeRepository;
        this.auditService = auditService;
    }

    public Unidade create(HttpServletRequest request, UnidadeCreateRequest dto) {
        Unidade u = new Unidade();
        u.setNome(dto.getNome());
        u.setUf(dto.getUf().toUpperCase());
        u.setCidade(dto.getCidade());
        u.setEndereco(dto.getEndereco());

        Unidade saved = unidadeRepository.save(u);
        auditService.log(request, "UNIDADE_CRIADA", "Unidade", saved.getId(), "Cadastro de unidade" );
        return saved;
    }

    public List<Unidade> list() {
        return unidadeRepository.findAll();
    }

    public Unidade get(Long id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new NotFoundException("Unidade não encontrada"));
    }

    public Unidade update(HttpServletRequest request, Long id, UnidadeUpdateRequest dto) {
        Unidade u = get(id);
        u.setNome(dto.getNome());
        u.setUf(dto.getUf().toUpperCase());
        u.setCidade(dto.getCidade());
        u.setEndereco(dto.getEndereco());

        Unidade saved = unidadeRepository.save(u);
        auditService.log(request, "UNIDADE_ATUALIZADA", "Unidade", id, "Atualização" );
        return saved;
    }

    public void delete(HttpServletRequest request, Long id) {
        Unidade u = get(id);
        unidadeRepository.delete(u);
        auditService.log(request, "UNIDADE_REMOVIDA", "Unidade", id, "Remoção" );
    }
}
