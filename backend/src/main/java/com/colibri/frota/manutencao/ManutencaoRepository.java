package com.colibri.frota.manutencao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {
    List<Manutencao> findByVeiculoIdOrderByDataDescIdDesc(Long veiculoId);
    Optional<Manutencao> findFirstByVeiculoIdAndParametroIdOrderByDataDescIdDesc(Long veiculoId, Long parametroId);
}
