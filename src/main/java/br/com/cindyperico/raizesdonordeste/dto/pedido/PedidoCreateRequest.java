package br.com.cindyperico.raizesdonordeste.dto.pedido;

import br.com.cindyperico.raizesdonordeste.model.enums.CanalAtendimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PedidoCreateRequest {

    private Long clienteId;

    @NotNull
    private Long unidadeId;

    @NotNull
    private CanalAtendimento canal;

    @Valid
    @NotEmpty
    private List<PedidoItemCreateRequest> itens;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
    }

    public CanalAtendimento getCanal() {
        return canal;
    }

    public void setCanal(CanalAtendimento canal) {
        this.canal = canal;
    }

    public List<PedidoItemCreateRequest> getItens() {
        return itens;
    }

    public void setItens(List<PedidoItemCreateRequest> itens) {
        this.itens = itens;
    }
}
