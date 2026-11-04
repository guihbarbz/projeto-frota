package com.colibri.frota.anomalia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnomaliaRequest(
        @NotNull Long veiculoId,
        @NotBlank @Size(max = 500) String descricao,
        @NotNull GravidadeAnomalia gravidade) {
}