package com.colibri.frota.veiculo;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {

    private final VeiculoRepository repo;

    public VeiculoController(VeiculoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        return repo.findByAtivoTrue().stream().map(VeiculoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscar(@PathVariable Long id) {
        return VeiculoResponse.de(achar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponse criar(@Valid @RequestBody VeiculoRequest req) {
        String placa = req.placa().trim().toUpperCase();
        if (repo.existsByPlaca(placa)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Placa já cadastrada");
        }
        Veiculo v = new Veiculo();
        preencher(v, req, placa);
        return VeiculoResponse.de(repo.save(v));
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest req) {
        Veiculo v = achar(id);
        String placa = req.placa().trim().toUpperCase();
        if (!placa.equals(v.getPlaca()) && repo.existsByPlaca(placa)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Placa já cadastrada");
        }
        preencher(v, req, placa);
        return VeiculoResponse.de(repo.save(v));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        Veiculo v = achar(id);
        v.setAtivo(false);
        repo.save(v);
    }

    private Veiculo achar(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
    }

    private void preencher(Veiculo v, VeiculoRequest req, String placa) {
        v.setPlaca(placa);
        v.setModelo(req.modelo());
        v.setAno(req.ano());
        v.setTipo(req.tipo());
        v.setKmAtual(req.kmAtual());
        v.setVencimentoIpva(req.vencimentoIpva());
        v.setVencimentoLicenciamento(req.vencimentoLicenciamento());
    }
}