package com.colibri.frota.parametro;

import com.colibri.frota.veiculo.TipoVeiculo;
import jakarta.persistence.*;

@Entity
@Table(name = "parametro_manutencao")
public class ParametroManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVeiculo tipoVeiculo;

    private Integer intervaloKm;
    private Integer intervaloDias;

    @Column(nullable = false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public TipoVeiculo getTipoVeiculo() { return tipoVeiculo; }
    public void setTipoVeiculo(TipoVeiculo tipoVeiculo) { this.tipoVeiculo = tipoVeiculo; }
    public Integer getIntervaloKm() { return intervaloKm; }
    public void setIntervaloKm(Integer intervaloKm) { this.intervaloKm = intervaloKm; }
    public Integer getIntervaloDias() { return intervaloDias; }
    public void setIntervaloDias(Integer intervaloDias) { this.intervaloDias = intervaloDias; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}