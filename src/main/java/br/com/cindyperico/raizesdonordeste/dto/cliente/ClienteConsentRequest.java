package br.com.cindyperico.raizesdonordeste.dto.cliente;

import jakarta.validation.constraints.NotNull;

public class ClienteConsentRequest {

    @NotNull
    private Boolean consentido;

    public Boolean getConsentido() {
        return consentido;
    }

    public void setConsentido(Boolean consentido) {
        this.consentido = consentido;
    }
}
