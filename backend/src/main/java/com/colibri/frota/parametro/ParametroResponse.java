package com.colibri.frota.parametro;

import com.colibri.frota.veiculo.TipoVeiculo;

public record ParametroResponse(Long id, String nome, TipoVeiculo tipoVeiculo,
                                Integer intervaloKm, Integer intervaloDias) {

    public static ParametroResponse de(ParametroManutencao p) {
        return new ParametroResponse(p.getId(), p.getNome(), p.getTipoVeiculo(),
                p.getIntervaloKm(), p.getIntervaloDias());
    }
}