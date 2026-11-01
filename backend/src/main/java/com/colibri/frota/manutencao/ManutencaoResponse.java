package com.colibri.frota.manutencao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ManutencaoResponse(Long id, Long veiculoId, String placa,
                                 Long parametroId, String tipoManutencao,
                                 LocalDate data, Integer kmNaData,
                                 BigDecimal custo, String observacao) {

    public static ManutencaoResponse de(Manutencao m) {
        return new ManutencaoResponse(m.getId(), m.getVeiculo().getId(), m.getVeiculo().getPlaca(),
                m.getParametro().getId(), m.getParametro().getNome(),
                m.getData(), m.getKmNaData(), m.getCusto(), m.getObservacao());
    }
}