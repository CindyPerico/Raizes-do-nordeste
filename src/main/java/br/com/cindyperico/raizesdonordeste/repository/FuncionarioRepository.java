package br.com.cindyperico.raizesdonordeste.repository;

import br.com.cindyperico.raizesdonordeste.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    List<Funcionario> findByUnidadeId(Long unidadeId);
}
