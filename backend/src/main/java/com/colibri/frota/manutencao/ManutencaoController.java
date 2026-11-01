package com.colibri.frota.manutencao;

import com.colibri.frota.parametro.ParametroManutencao;
import com.colibri.frota.parametro.ParametroManutencaoRepository;
import com.colibri.frota.veiculo.Veiculo;
import com.colibri.frota.veiculo.VeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/manutencoes")
public class ManutencaoController {

    private final ManutencaoRepository repo;
    private final VeiculoRepository veiculoRepo;
    private final ParametroManutencaoRepository parametroRepo;

    public ManutencaoController(ManutencaoRepository repo, VeiculoRepository veiculoRepo,
                                ParametroManutencaoRepository parametroRepo) {
        this.repo = repo;
        this.veiculoRepo = veiculoRepo;
        this.parametroRepo = parametroRepo;
    }

    @GetMapping
    public List<ManutencaoResponse> listar(@RequestParam(required = false) Long veiculoId) {
        List<Manutencao> lista = (veiculoId == null)
                ? repo.findAll(Sort.by(Sort.Direction.DESC, "data", "id"))
                : repo.findByVeiculoIdOrderByDataDescIdDesc(veiculoId);
        return lista.stream().map(ManutencaoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ManutencaoResponse registrar(@Valid @RequestBody ManutencaoRequest req) {
        Veiculo v = veiculoRepo.findById(req.veiculoId())
                .filter(Veiculo::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Veículo não encontrado"));
        ParametroManutencao p = parametroRepo.findById(req.parametroId())
                .filter(ParametroManutencao::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parâmetro de manutenção não encontrado"));

        if (p.getTipoVeiculo() != v.getTipo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esse tipo de manutenção não se aplica a " + v.getTipo());
        }

        Manutencao m = new Manutencao();
        m.setVeiculo(v);
        m.setParametro(p);
        m.setData(req.data());
        m.setKmNaData(req.kmNaData());
        m.setCusto(req.custo());
        m.setObservacao(req.observacao());

        // se a manutenção foi feita com km maior que o atual, o veículo avançou
        if (req.kmNaData() > v.getKmAtual()) {
            v.setKmAtual(req.kmNaData());
            veiculoRepo.save(v);
        }
        return ManutencaoResponse.de(repo.save(m));
    }
}