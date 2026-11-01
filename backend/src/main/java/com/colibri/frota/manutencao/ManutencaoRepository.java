package com.colibri.frota.manutencao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {
    List<Manutencao> findByVeiculoIdOrderByDataDescIdDesc(Long veiculoId);
}