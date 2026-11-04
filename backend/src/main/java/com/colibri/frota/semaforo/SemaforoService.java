package com.colibri.frota.semaforo;

import com.colibri.frota.anomalia.Anomalia;
import com.colibri.frota.anomalia.AnomaliaRepository;
import com.colibri.frota.anomalia.GravidadeAnomalia;
import com.colibri.frota.anomalia.StatusAnomalia;
import com.colibri.frota.manutencao.Manutencao;
import com.colibri.frota.manutencao.ManutencaoRepository;
import com.colibri.frota.parametro.ParametroManutencao;
import com.colibri.frota.parametro.ParametroManutencaoRepository;
import com.colibri.frota.veiculo.Veiculo;
import com.colibri.frota.veiculo.VeiculoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.colibri.frota.semaforo.StatusSemaforo.*;

@Service
public class SemaforoService {

    private final VeiculoRepository veiculoRepo;
    private final ParametroManutencaoRepository parametroRepo;
    private final ManutencaoRepository manutencaoRepo;
    private final AnomaliaRepository anomaliaRepo;
    private final int margemKmPercentual;
    private final int margemDias;

    public SemaforoService(VeiculoRepository veiculoRepo,
                           ParametroManutencaoRepository parametroRepo,
                           ManutencaoRepository manutencaoRepo,
                           AnomaliaRepository anomaliaRepo,
                           @Value("${frota.semaforo.margem-km-percentual:10}") int margemKmPercentual,
                           @Value("${frota.semaforo.margem-dias:15}") int margemDias) {
        this.veiculoRepo = veiculoRepo;
        this.parametroRepo = parametroRepo;
        this.manutencaoRepo = manutencaoRepo;
        this.anomaliaRepo = anomaliaRepo;
        this.margemKmPercentual = margemKmPercentual;
        this.margemDias = margemDias;
    }

    public List<SemaforoVeiculo> todos() {
        LocalDate hoje = LocalDate.now();
        List<ParametroManutencao> parametros = parametroRepo.findByAtivoTrue();
        return veiculoRepo.findByAtivoTrue().stream()
                .map(v -> calcular(v, parametros, hoje))
                .sorted(Comparator.comparing(SemaforoVeiculo::status).reversed()
                        .thenComparing(SemaforoVeiculo::placa))
                .toList();
    }

    public SemaforoVeiculo porVeiculo(Long id) {
        Veiculo v = veiculoRepo.findById(id)
                .filter(Veiculo::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Veículo não encontrado"));
        return calcular(v, parametroRepo.findByAtivoTrue(), LocalDate.now());
    }

    private SemaforoVeiculo calcular(Veiculo v, List<ParametroManutencao> parametros, LocalDate hoje) {
        List<Pendencia> pendencias = new ArrayList<>();

        for (ParametroManutencao p : parametros) {
            if (p.getTipoVeiculo() != v.getTipo()) continue;
            Optional<Manutencao> ultima = manutencaoRepo
                    .findFirstByVeiculoIdAndParametroIdOrderByDataDescIdDesc(v.getId(), p.getId());
            if (ultima.isEmpty()) {
                pendencias.add(new Pendencia(p.getNome(), AMARELO,
                        p.getNome() + ": sem registro, cadastre a última manutenção", null, null));
            } else {
                avaliarManutencao(v, p, ultima.get(), hoje).ifPresent(pendencias::add);
            }
        }
        avaliarDocumento("IPVA", v.getVencimentoIpva(), hoje).ifPresent(pendencias::add);
        avaliarDocumento("Licenciamento", v.getVencimentoLicenciamento(), hoje).ifPresent(pendencias::add);

        // anomalias abertas: gravidade ALTA deixa vermelho, as demais deixam amarelo
        for (Anomalia a : anomaliaRepo.findByVeiculoIdAndStatus(v.getId(), StatusAnomalia.ABERTA)) {
            StatusSemaforo s = (a.getGravidade() == GravidadeAnomalia.ALTA) ? VERMELHO : AMARELO;
            pendencias.add(new Pendencia("Anomalia", s,
                    "Anomalia aberta (gravidade " + a.getGravidade() + "): " + a.getDescricao(),
                    null, null));
        }

        pendencias.sort(Comparator.comparing(Pendencia::status).reversed());
        StatusSemaforo geral = pendencias.isEmpty() ? VERDE : pendencias.get(0).status();

        return new SemaforoVeiculo(v.getId(), v.getPlaca(), v.getModelo(), v.getTipo(),
                v.getKmAtual(), geral, pendencias);
    }

    private Optional<Pendencia> avaliarManutencao(Veiculo v, ParametroManutencao p,
                                                  Manutencao ultima, LocalDate hoje) {
        StatusSemaforo status = VERDE;
        List<String> textos = new ArrayList<>();
        Integer kmRestantes = null;
        Integer diasRestantes = null;

        if (p.getIntervaloKm() != null) {
            kmRestantes = ultima.getKmNaData() + p.getIntervaloKm() - v.getKmAtual();
            int margemKm = p.getIntervaloKm() * margemKmPercentual / 100;
            StatusSemaforo s = classificar(kmRestantes, margemKm);
            if (s != VERDE) textos.add(textoKm(kmRestantes));
            status = pior(status, s);
        }
        if (p.getIntervaloDias() != null) {
            diasRestantes = (int) ChronoUnit.DAYS.between(hoje,
                    ultima.getData().plusDays(p.getIntervaloDias()));
            StatusSemaforo s = classificar(diasRestantes, margemDias);
            if (s != VERDE) textos.add(textoDias(diasRestantes));
            status = pior(status, s);
        }
        if (status == VERDE) return Optional.empty();
        return Optional.of(new Pendencia(p.getNome(), status,
                p.getNome() + ": " + String.join(" e ", textos), kmRestantes, diasRestantes));
    }

    private Optional<Pendencia> avaliarDocumento(String nome, LocalDate vencimento, LocalDate hoje) {
        if (vencimento == null) return Optional.empty();
        int dias = (int) ChronoUnit.DAYS.between(hoje, vencimento);
        StatusSemaforo s = classificar(dias, margemDias);
        if (s == VERDE) return Optional.empty();
        String motivo = dias < 0 ? nome + " vencido há " + dias(-dias)
                : dias == 0 ? nome + " vence hoje"
                  : nome + " vence em " + dias(dias);
        return Optional.of(new Pendencia(nome, s, motivo, null, dias));
    }

    private StatusSemaforo classificar(int restante, int margem) {
        if (restante <= 0) return VERMELHO;
        if (restante <= margem) return AMARELO;
        return VERDE;
    }

    private StatusSemaforo pior(StatusSemaforo a, StatusSemaforo b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    private String textoKm(int restante) {
        if (restante < 0) return "passou " + (-restante) + " km do prazo";
        if (restante == 0) return "chegou ao limite de km";
        return "faltam " + restante + " km";
    }

    private String textoDias(int restante) {
        if (restante < 0) return "passou do prazo há " + dias(-restante);
        if (restante == 0) return "vence hoje";
        return "faltam " + dias(restante);
    }

    private String dias(int n) {
        return n + (n == 1 ? " dia" : " dias");
    }
}