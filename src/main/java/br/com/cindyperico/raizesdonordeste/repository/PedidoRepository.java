package br.com.cindyperico.raizesdonordeste.repository;

import br.com.cindyperico.raizesdonordeste.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
