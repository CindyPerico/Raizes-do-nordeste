package br.com.cindyperico.raizesdonordeste.service;

import br.com.cindyperico.raizesdonordeste.model.AuditLog;
import br.com.cindyperico.raizesdonordeste.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(HttpServletRequest request, String acao, String entidade, Long entidadeId, String detalhes) {
        AuditLog log = new AuditLog();
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setDetalhes(detalhes);
        log.setUsuario((String) request.getAttribute("audit.username"));
        log.setIp(request.getRemoteAddr());
        auditLogRepository.save(log);
    }
}
