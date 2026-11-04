package com.colibri.frota.anomalia;

import com.colibri.frota.usuario.Usuario;
import com.colibri.frota.usuario.UsuarioRepository;
import com.colibri.frota.veiculo.Veiculo;
import com.colibri.frota.veiculo.VeiculoRepository;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/anomalias")
public class AnomaliaController {

    private final AnomaliaRepository repo;
    private final VeiculoRepository veiculoRepo;
    private final UsuarioRepository usuarioRepo;
    private final ArmazenamentoFotos fotos;

    public AnomaliaController(AnomaliaRepository repo, VeiculoRepository veiculoRepo,
                              UsuarioRepository usuarioRepo, ArmazenamentoFotos fotos) {
        this.repo = repo;
        this.veiculoRepo = veiculoRepo;
        this.usuarioRepo = usuarioRepo;
        this.fotos = fotos;
    }

    @GetMapping
    public List<AnomaliaResponse> listar(@RequestParam(required = false) Long veiculoId,
                                         @RequestParam(required = false) StatusAnomalia status) {
        List<Anomalia> base = (veiculoId == null)
                ? repo.findAllByOrderByCriadaEmDesc()
                : repo.findByVeiculoIdOrderByCriadaEmDesc(veiculoId);
        return base.stream()
                .filter(a -> status == null || a.getStatus() == status)
                .map(AnomaliaResponse::de)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnomaliaResponse reportar(@Valid @RequestBody AnomaliaRequest req, Authentication auth) {
        Veiculo v = veiculoRepo.findById(req.veiculoId())
                .filter(Veiculo::isAtivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Veículo não encontrado"));
        Usuario u = usuarioRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String descricao = req.descricao().trim();
        if (repo.existsByVeiculoIdAndStatusAndDescricaoIgnoreCase(v.getId(), StatusAnomalia.ABERTA, descricao)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma anomalia aberta com essa descrição para este veículo");
        }

        Anomalia a = new Anomalia();
        a.setVeiculo(v);
        a.setReportadoPor(u);
        a.setDescricao(descricao);
        a.setGravidade(req.gravidade());
        return AnomaliaResponse.de(repo.save(a));
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnomaliaResponse anexarFoto(@PathVariable Long id,
                                       @RequestParam("arquivo") MultipartFile arquivo) {
        Anomalia a = achar(id);
        a.setFotoArquivo(fotos.salvar(arquivo));
        return AnomaliaResponse.de(repo.save(a));
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> foto(@PathVariable Long id) {
        Anomalia a = achar(id);
        if (a.getFotoArquivo() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esta anomalia não tem foto");
        }
        return ResponseEntity.ok()
                .contentType(ArmazenamentoFotos.tipo(a.getFotoArquivo()))
                .body(fotos.carregar(a.getFotoArquivo()));
    }

    @PutMapping("/{id}/resolver")
    public AnomaliaResponse resolver(@PathVariable Long id,
                                     @Valid @RequestBody(required = false) ResolverRequest req) {
        Anomalia a = achar(id);
        if (a.getStatus() != StatusAnomalia.RESOLVIDA) {
            a.setStatus(StatusAnomalia.RESOLVIDA);
            a.setResolvidaEm(Instant.now());
        }
        if (req != null && req.nota() != null) {
            a.setNotaResolucao(req.nota());
        }
        return AnomaliaResponse.de(repo.save(a));
    }

    private Anomalia achar(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Anomalia não encontrada"));
    }
}