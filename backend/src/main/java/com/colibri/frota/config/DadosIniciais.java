package com.colibri.frota.config;

import com.colibri.frota.parametro.ParametroManutencao;
import com.colibri.frota.parametro.ParametroManutencaoRepository;
import com.colibri.frota.veiculo.TipoVeiculo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DadosIniciais implements CommandLineRunner {

    private final ParametroManutencaoRepository repo;

    public DadosIniciais(ParametroManutencaoRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        criar("Troca de óleo", TipoVeiculo.CARRO, 1000, 90);
        criar("Revisão geral", TipoVeiculo.CARRO, 10000, 365);
        criar("Troca de óleo", TipoVeiculo.MOTO, 3000, 90);
        criar("Revisão geral", TipoVeiculo.MOTO, 6000, 365);
        criar("Lubrificação e ajuste da corrente", TipoVeiculo.MOTO, 1000, null);
    }

    private void criar(String nome, TipoVeiculo tipo, Integer km, Integer dias) {
        ParametroManutencao p = new ParametroManutencao();
        p.setNome(nome);
        p.setTipoVeiculo(tipo);
        p.setIntervaloKm(km);
        p.setIntervaloDias(dias);
        repo.save(p);
    }
}