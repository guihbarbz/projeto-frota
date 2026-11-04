package com.colibri.frota.anomalia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnomaliaRepository extends JpaRepository<Anomalia, Long> {
    List<Anomalia> findByVeiculoIdAndStatus(Long veiculoId, StatusAnomalia status);
    List<Anomalia> findByVeiculoIdOrderByCriadaEmDesc(Long veiculoId);
    List<Anomalia> findAllByOrderByCriadaEmDesc();
    boolean existsByVeiculoIdAndStatusAndDescricaoIgnoreCase(Long veiculoId, StatusAnomalia status, String descricao);
}