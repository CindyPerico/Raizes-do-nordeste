package br.com.cindyperico.raizesdonordeste.dto.pedido;

import br.com.cindyperico.raizesdonordeste.model.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public class PedidoStatusUpdateRequest {

    @NotNull
    private StatusPedido status;

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}
