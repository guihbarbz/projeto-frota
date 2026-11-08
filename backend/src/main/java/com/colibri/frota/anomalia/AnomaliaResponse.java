package com.colibri.frota.anomalia;

import java.time.Instant;

public record AnomaliaResponse(Long id, Long veiculoId, String placa, String reportadoPor,
                               String descricao, GravidadeAnomalia gravidade, StatusAnomalia status,
                               String fotoUrl, Instant criadaEm, Instant resolvidaEm,
                               String notaResolucao) {

    public static AnomaliaResponse de(Anomalia a) {
        String foto = a.isTemFoto() ? "/api/anomalias/" + a.getId() + "/foto" : null;
        return new AnomaliaResponse(a.getId(), a.getVeiculo().getId(), a.getVeiculo().getPlaca(),
                a.getReportadoPor().getNome(), a.getDescricao(), a.getGravidade(), a.getStatus(),
                foto, a.getCriadaEm(), a.getResolvidaEm(), a.getNotaResolucao());
    }
}