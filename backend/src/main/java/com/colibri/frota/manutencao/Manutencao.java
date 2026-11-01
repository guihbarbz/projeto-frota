package com.colibri.frota.manutencao;

import com.colibri.frota.parametro.ParametroManutencao;
import com.colibri.frota.veiculo.Veiculo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "manutencao")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Veiculo veiculo;

    @ManyToOne(optional = false)
    private ParametroManutencao parametro;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private Integer kmNaData;

    private BigDecimal custo;

    @Column(length = 500)
    private String observacao;

    public Long getId() { return id; }
    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }
    public ParametroManutencao getParametro() { return parametro; }
    public void setParametro(ParametroManutencao parametro) { this.parametro = parametro; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getKmNaData() { return kmNaData; }
    public void setKmNaData(Integer kmNaData) { this.kmNaData = kmNaData; }
    public BigDecimal getCusto() { return custo; }
    public void setCusto(BigDecimal custo) { this.custo = custo; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}