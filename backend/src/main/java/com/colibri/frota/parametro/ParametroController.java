package com.colibri.frota.parametro;

import com.colibri.frota.veiculo.TipoVeiculo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    private final ParametroManutencaoRepository repo;

    public ParametroController(ParametroManutencaoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ParametroResponse> listar(@RequestParam(required = false) TipoVeiculo tipo) {
        List<ParametroManutencao> lista = (tipo == null)
                ? repo.findByAtivoTrue()
                : repo.findByTipoVeiculoAndAtivoTrue(tipo);
        return lista.stream().map(ParametroResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParametroResponse criar(@Valid @RequestBody ParametroRequest req) {
        exigirAlgumIntervalo(req);
        ParametroManutencao p = new ParametroManutencao();
        preencher(p, req);
        return ParametroResponse.de(repo.save(p));
    }

    @PutMapping("/{id}")
    public ParametroResponse atualizar(@PathVariable Long id, @Valid @RequestBody ParametroRequest req) {
        exigirAlgumIntervalo(req);
        ParametroManutencao p = achar(id);
        preencher(p, req);
        return ParametroResponse.de(repo.save(p));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        ParametroManutencao p = achar(id);
        p.setAtivo(false);
        repo.save(p);
    }

    private ParametroManutencao achar(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Parâmetro não encontrado"));
    }

    private void exigirAlgumIntervalo(ParametroRequest req) {
        if (req.intervaloKm() == null && req.intervaloDias() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe intervaloKm, intervaloDias ou os dois");
        }
    }

    private void preencher(ParametroManutencao p, ParametroRequest req) {
        p.setNome(req.nome());
        p.setTipoVeiculo(req.tipoVeiculo());
        p.setIntervaloKm(req.intervaloKm());
        p.setIntervaloDias(req.intervaloDias());
    }
}