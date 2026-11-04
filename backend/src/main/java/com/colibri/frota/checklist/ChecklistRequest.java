package com.colibri.frota.checklist;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ChecklistRequest(
        @NotNull Long veiculoId,
        @NotNull @PositiveOrZero Integer km,
        @Size(max = 500) String observacao) {
}