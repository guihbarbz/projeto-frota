package com.colibri.frota.veiculo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "veiculo")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 7)
    private String placa;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVeiculo tipo;

    @Column(nullable = false)
    private Integer kmAtual;

    private LocalDate vencimentoIpva;
    private LocalDate vencimentoLicenciamento;

    @Column(nullable = false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public TipoVeiculo getTipo() { return tipo; }
    public void setTipo(TipoVeiculo tipo) { this.tipo = tipo; }
    public Integer getKmAtual() { return kmAtual; }
    public void setKmAtual(Integer kmAtual) { this.kmAtual = kmAtual; }
    public LocalDate getVencimentoIpva() { return vencimentoIpva; }
    public void setVencimentoIpva(LocalDate v) { this.vencimentoIpva = v; }
    public LocalDate getVencimentoLicenciamento() { return vencimentoLicenciamento; }
    public void setVencimentoLicenciamento(LocalDate v) { this.vencimentoLicenciamento = v; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}