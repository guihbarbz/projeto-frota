package com.colibri.frota.veiculo;

import java.time.LocalDate;

public record VeiculoResponse(Long id, String placa, String modelo, Integer ano,
                              TipoVeiculo tipo, Integer kmAtual,
                              LocalDate vencimentoIpva, LocalDate vencimentoLicenciamento,
                              boolean ativo) {

    public static VeiculoResponse de(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getPlaca(), v.getModelo(), v.getAno(),
                v.getTipo(), v.getKmAtual(), v.getVencimentoIpva(),
                v.getVencimentoLicenciamento(), v.isAtivo());
    }
}