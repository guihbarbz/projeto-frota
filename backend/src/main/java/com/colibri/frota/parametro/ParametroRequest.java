package com.colibri.frota.parametro;

import com.colibri.frota.veiculo.TipoVeiculo;
import jakarta.validation.constraints.*;

public record ParametroRequest(
        @NotBlank String nome,
        @NotNull TipoVeiculo tipoVeiculo,
        @Positive Integer intervaloKm,
        @Positive Integer intervaloDias) {
}