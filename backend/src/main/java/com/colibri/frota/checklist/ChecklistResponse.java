package com.colibri.frota.checklist;

import java.time.Instant;

public record ChecklistResponse(Long id, Long veiculoId, String placa, String instrutor,
                                Instant dataHora, Integer km, String observacao) {

    public static ChecklistResponse de(RegistroChecklist c) {
        return new ChecklistResponse(c.getId(), c.getVeiculo().getId(), c.getVeiculo().getPlaca(),
                c.getInstrutor().getNome(), c.getDataHora(), c.getKm(), c.getObservacao());
    }
}