package com.colibri.frota.consulta;

import com.colibri.frota.anomalia.AnomaliaResponse;
import com.colibri.frota.checklist.ChecklistResponse;
import com.colibri.frota.manutencao.ManutencaoResponse;
import com.colibri.frota.semaforo.Pendencia;
import com.colibri.frota.semaforo.StatusSemaforo;
import com.colibri.frota.veiculo.TipoVeiculo;
import java.util.List;

public record HistoricoVeiculo(Long veiculoId, String placa, String modelo, TipoVeiculo tipo,
                               Integer kmAtual, StatusSemaforo status, List<Pendencia> pendencias,
                               List<ManutencaoResponse> manutencoes,
                               List<ChecklistResponse> checklistsRecentes,
                               List<AnomaliaResponse> anomalias) {
}