package com.colibri.frota.semaforo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/semaforo")
public class SemaforoController {

    private final SemaforoService service;

    public SemaforoController(SemaforoService service) {
        this.service = service;
    }

    @GetMapping
    public List<SemaforoVeiculo> todos() {
        return service.todos();
    }

    @GetMapping("/{veiculoId}")
    public SemaforoVeiculo porVeiculo(@PathVariable Long veiculoId) {
        return service.porVeiculo(veiculoId);
    }
}