package com.colibri.frota.semaforo;

import com.colibri.frota.veiculo.TipoVeiculo;
import java.util.List;

public record SemaforoVeiculo(Long veiculoId, String placa, String modelo, TipoVeiculo tipo,
                              Integer kmAtual, StatusSemaforo status, List<Pendencia> pendencias) {
}