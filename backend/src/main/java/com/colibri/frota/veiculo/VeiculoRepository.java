package com.colibri.frota.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    List<Veiculo> findByAtivoTrue();
    boolean existsByPlaca(String placa);
}