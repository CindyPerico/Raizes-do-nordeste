package br.com.cindyperico.raizesdonordeste.dto.auth;

import br.com.cindyperico.raizesdonordeste.model.enums.Role;

public record LoginResponse(String accessToken, String tokenType, long expiresInMs, String email, Role role) {
}
