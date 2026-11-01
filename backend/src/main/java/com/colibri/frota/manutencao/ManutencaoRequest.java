package com.colibri.frota.manutencao;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ManutencaoRequest(
        @NotNull Long veiculoId,
        @NotNull Long parametroId,
        @NotNull LocalDate data,
        @NotNull @PositiveOrZero Integer kmNaData,
        @PositiveOrZero BigDecimal custo,
        @Size(max = 500) String observacao) {
}