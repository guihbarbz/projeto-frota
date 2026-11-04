package com.colibri.frota.checklist;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChecklistRepository extends JpaRepository<RegistroChecklist, Long> {
    List<RegistroChecklist> findTop50ByOrderByDataHoraDesc();
    List<RegistroChecklist> findTop50ByVeiculoIdOrderByDataHoraDesc(Long veiculoId);
}