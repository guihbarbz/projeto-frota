package com.colibri.frota.consulta;

import com.colibri.frota.veiculo.TipoVeiculo;
import java.time.LocalDate;

public record VencimentoResponse(Long veiculoId, String placa, String modelo, TipoVeiculo tipo,
                                 String documento, LocalDate dataVencimento,
                                 int diasRestantes, String situacao) {
}