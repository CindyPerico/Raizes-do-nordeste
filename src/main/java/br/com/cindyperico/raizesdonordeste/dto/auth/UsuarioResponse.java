package br.com.cindyperico.raizesdonordeste.dto.auth;

import br.com.cindyperico.raizesdonordeste.model.enums.Role;

public record UsuarioResponse(Long id, String nome, String email, Role role, Long unidadeId, Long clienteId) {
}
