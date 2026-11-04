package com.colibri.frota.checklist;

import com.colibri.frota.usuario.Usuario;
import com.colibri.frota.usuario.UsuarioRepository;
import com.colibri.frota.veiculo.Veiculo;
import com.colibri.frota.veiculo.VeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    private final ChecklistRepository repo;
    private final VeiculoRepository veiculoRepo;
    private final UsuarioRepository usuarioRepo;

    public ChecklistController(ChecklistRepository repo, VeiculoRepository veiculoRepo,
                               UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.veiculoRepo = veiculoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public List<ChecklistResponse> listar(@RequestParam(required = false) Long veiculoId) {
        List<RegistroChecklist> lista = (veiculoId == null)
                ? repo.findTop50ByOrderByDataHoraDesc()
                : repo.findTop50ByVeiculoIdOrderByDataHoraDesc(veiculoId);
        return lista.stream().map(ChecklistResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ChecklistResponse registrar(@Valid @RequestBody ChecklistRequest req, Authentication auth) {
        Veiculo v = veiculoRepo.findById(req.veiculoId())
                .filter(Veiculo::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Veículo não encontrado"));
        Usuario u = usuarioRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (req.km() < v.getKmAtual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O km informado (" + req.km() + ") é menor que o km atual do veículo ("
                            + v.getKmAtual() + "). Confira o valor.");
        }

        RegistroChecklist r = new RegistroChecklist();
        r.setVeiculo(v);
        r.setInstrutor(u);
        r.setKm(req.km());
        r.setObservacao(req.observacao());

        v.setKmAtual(req.km());
        veiculoRepo.save(v);
        return ChecklistResponse.de(repo.save(r));
    }
}