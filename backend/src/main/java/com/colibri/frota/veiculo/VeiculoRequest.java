package com.colibri.frota.veiculo;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record VeiculoRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}[0-9][A-Za-z0-9][0-9]{2}$",
                message = "placa inválida (ex.: ABC1234 ou ABC1D23)") String placa,
        @NotBlank String modelo,
        @NotNull @Min(1950) Integer ano,
        @NotNull TipoVeiculo tipo,
        @NotNull @PositiveOrZero Integer kmAtual,
        LocalDate vencimentoIpva,
        LocalDate vencimentoLicenciamento) {
}