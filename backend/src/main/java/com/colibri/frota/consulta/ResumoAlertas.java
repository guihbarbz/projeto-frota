package com.colibri.frota.consulta;

import com.colibri.frota.semaforo.SemaforoVeiculo;
import java.time.LocalDate;
import java.util.List;

public record ResumoAlertas(LocalDate geradoEm, int totalVeiculos,
                            int vermelhos, int amarelos, int verdes,
                            List<SemaforoVeiculo> veiculosComAlerta) {
}