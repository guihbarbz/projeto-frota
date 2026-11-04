package com.colibri.frota.checklist;

import com.colibri.frota.usuario.Usuario;
import com.colibri.frota.veiculo.Veiculo;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "registro_checklist")
public class RegistroChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Veiculo veiculo;

    @ManyToOne(optional = false)
    private Usuario instrutor;

    @Column(nullable = false)
    private Instant dataHora = Instant.now();

    @Column(nullable = false)
    private Integer km;

    @Column(length = 500)
    private String observacao;

    public Long getId() { return id; }
    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }
    public Usuario getInstrutor() { return instrutor; }
    public void setInstrutor(Usuario instrutor) { this.instrutor = instrutor; }
    public Instant getDataHora() { return dataHora; }
    public Integer getKm() { return km; }
    public void setKm(Integer km) { this.km = km; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}