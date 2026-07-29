package br.com.cindyperico.raizesdonordeste.repository;

import br.com.cindyperico.raizesdonordeste.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}
