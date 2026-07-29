package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.dto.funcionario.FuncionarioCreateRequest;
import br.com.cindyperico.raizesdonordeste.dto.funcionario.FuncionarioUpdateRequest;
import br.com.cindyperico.raizesdonordeste.model.Funcionario;
import br.com.cindyperico.raizesdonordeste.model.Unidade;
import br.com.cindyperico.raizesdonordeste.repository.FuncionarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final UnidadeService unidadeService;
    private final AuditService auditService;

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              UnidadeService unidadeService,
                              AuditService auditService) {
        this.funcionarioRepository = funcionarioRepository;
        this.unidadeService = unidadeService;
        this.auditService = auditService;
    }

    public Funcionario create(HttpServletRequest request, FuncionarioCreateRequest dto) {
        Unidade unidade = unidadeService.get(dto.getUnidadeId());

        Funcionario f = new Funcionario();
        f.setNome(dto.getNome());
        f.setCargo(dto.getCargo());
        f.setUnidade(unidade);
        f.setAtivo(true);

        Funcionario saved = funcionarioRepository.save(f);
        auditService.log(request, "FUNCIONARIO_CRIADO", "Funcionario", saved.getId(), "UnidadeId=" + unidade.getId());
        return saved;
    }

    public List<Funcionario> list() {
        return funcionarioRepository.findAll();
    }

    public List<Funcionario> listByUnidade(Long unidadeId) {
        unidadeService.get(unidadeId);
        return funcionarioRepository.findByUnidadeId(unidadeId);
    }

    public Funcionario get(Long id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
    }

    public Funcionario update(HttpServletRequest request, Long id, FuncionarioUpdateRequest dto) {
        Funcionario f = get(id);
        f.setNome(dto.getNome());
        f.setCargo(dto.getCargo());
        if (dto.getAtivo() != null) {
            f.setAtivo(dto.getAtivo());
        }

        Funcionario saved = funcionarioRepository.save(f);
        auditService.log(request, "FUNCIONARIO_ATUALIZADO", "Funcionario", id, "Atualização" );
        return saved;
    }

    public void delete(HttpServletRequest request, Long id) {
        Funcionario f = get(id);
        funcionarioRepository.delete(f);
        auditService.log(request, "FUNCIONARIO_REMOVIDO", "Funcionario", id, "Remoção" );
    }
}
