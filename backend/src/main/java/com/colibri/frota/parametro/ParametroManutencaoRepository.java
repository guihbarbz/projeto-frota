package com.colibri.frota.parametro;

import com.colibri.frota.veiculo.TipoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParametroManutencaoRepository extends JpaRepository<ParametroManutencao, Long> {
    List<ParametroManutencao> findByAtivoTrue();
    List<ParametroManutencao> findByTipoVeiculoAndAtivoTrue(TipoVeiculo tipo);
}