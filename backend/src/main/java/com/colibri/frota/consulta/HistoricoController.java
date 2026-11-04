package com.colibri.frota.consulta;

import com.colibri.frota.anomalia.AnomaliaRepository;
import com.colibri.frota.anomalia.AnomaliaResponse;
import com.colibri.frota.checklist.ChecklistRepository;
import com.colibri.frota.checklist.ChecklistResponse;
import com.colibri.frota.manutencao.ManutencaoRepository;
import com.colibri.frota.manutencao.ManutencaoResponse;
import com.colibri.frota.semaforo.SemaforoService;
import com.colibri.frota.semaforo.SemaforoVeiculo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/veiculos")
public class HistoricoController {

    private final SemaforoService semaforo;
    private final ManutencaoRepository manutencoes;
    private final ChecklistRepository checklists;
    private final AnomaliaRepository anomalias;

    public HistoricoController(SemaforoService semaforo, ManutencaoRepository manutencoes,
                               ChecklistRepository checklists, AnomaliaRepository anomalias) {
        this.semaforo = semaforo;
        this.manutencoes = manutencoes;
        this.checklists = checklists;
        this.anomalias = anomalias;
    }

    @GetMapping("/{id}/historico")
    public HistoricoVeiculo historico(@PathVariable Long id) {
        SemaforoVeiculo s = semaforo.porVeiculo(id); // devolve 404 se o veículo não existir
        return new HistoricoVeiculo(s.veiculoId(), s.placa(), s.modelo(), s.tipo(), s.kmAtual(),
                s.status(), s.pendencias(),
                manutencoes.findByVeiculoIdOrderByDataDescIdDesc(id).stream()
                        .map(ManutencaoResponse::de).toList(),
                checklists.findTop50ByVeiculoIdOrderByDataHoraDesc(id).stream()
                        .map(ChecklistResponse::de).toList(),
                anomalias.findByVeiculoIdOrderByCriadaEmDesc(id).stream()
                        .map(AnomaliaResponse::de).toList());
    }
}