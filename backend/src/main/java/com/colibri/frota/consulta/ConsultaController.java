package com.colibri.frota.consulta;

import com.colibri.frota.semaforo.SemaforoService;
import com.colibri.frota.semaforo.SemaforoVeiculo;
import com.colibri.frota.semaforo.StatusSemaforo;
import com.colibri.frota.veiculo.Veiculo;
import com.colibri.frota.veiculo.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ConsultaController {

    private final SemaforoService semaforo;
    private final VeiculoRepository veiculos;

    public ConsultaController(SemaforoService semaforo, VeiculoRepository veiculos) {
        this.semaforo = semaforo;
        this.veiculos = veiculos;
    }

    @GetMapping("/alertas/resumo")
    public ResumoAlertas resumo() {
        List<SemaforoVeiculo> todos = semaforo.todos();
        int vermelhos = contar(todos, StatusSemaforo.VERMELHO);
        int amarelos = contar(todos, StatusSemaforo.AMARELO);
        int verdes = contar(todos, StatusSemaforo.VERDE);
        List<SemaforoVeiculo> comAlerta = todos.stream()
                .filter(s -> s.status() != StatusSemaforo.VERDE)
                .toList();
        return new ResumoAlertas(LocalDate.now(), todos.size(), vermelhos, amarelos, verdes, comAlerta);
    }

    @GetMapping("/vencimentos")
    public List<VencimentoResponse> vencimentos(@RequestParam(defaultValue = "30") int dias) {
        if (dias < 1 || dias > 365) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "dias deve estar entre 1 e 365");
        }
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        List<VencimentoResponse> lista = new ArrayList<>();
        for (Veiculo v : veiculos.findByAtivoTrue()) {
            adicionar(lista, v, "IPVA", v.getVencimentoIpva(), hoje, limite);
            adicionar(lista, v, "Licenciamento", v.getVencimentoLicenciamento(), hoje, limite);
        }
        lista.sort(Comparator.comparing(VencimentoResponse::dataVencimento));
        return lista;
    }

    private int contar(List<SemaforoVeiculo> lista, StatusSemaforo status) {
        return (int) lista.stream().filter(s -> s.status() == status).count();
    }

    private void adicionar(List<VencimentoResponse> lista, Veiculo v, String documento,
                           LocalDate data, LocalDate hoje, LocalDate limite) {
        if (data == null || data.isAfter(limite)) return;
        int restantes = (int) ChronoUnit.DAYS.between(hoje, data);
        String situacao = restantes < 0 ? "VENCIDO"
                : restantes == 0 ? "VENCE_HOJE" : "VENCE_EM_BREVE";
        lista.add(new VencimentoResponse(v.getId(), v.getPlaca(), v.getModelo(), v.getTipo(),
                documento, data, restantes, situacao));
    }
}