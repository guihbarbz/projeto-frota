package com.colibri.frota.anomalia;

import com.colibri.frota.usuario.Usuario;
import com.colibri.frota.veiculo.Veiculo;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "anomalia")
public class Anomalia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Veiculo veiculo;

    @ManyToOne(optional = false)
    private Usuario reportadoPor;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GravidadeAnomalia gravidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAnomalia status = StatusAnomalia.ABERTA;

    // a foto em si fica na tabela foto_anomalia; aqui só o indicador
    @Column(name = "tem_foto", nullable = false, columnDefinition = "boolean default false")
    private boolean temFoto;

    @Column(nullable = false)
    private Instant criadaEm = Instant.now();

    private Instant resolvidaEm;

    @Column(length = 500)
    private String notaResolucao;

    public Long getId() { return id; }
    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }
    public Usuario getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(Usuario reportadoPor) { this.reportadoPor = reportadoPor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public GravidadeAnomalia getGravidade() { return gravidade; }
    public void setGravidade(GravidadeAnomalia gravidade) { this.gravidade = gravidade; }
    public StatusAnomalia getStatus() { return status; }
    public void setStatus(StatusAnomalia status) { this.status = status; }
    public boolean isTemFoto() { return temFoto; }
    public void setTemFoto(boolean temFoto) { this.temFoto = temFoto; }
    public Instant getCriadaEm() { return criadaEm; }
    public Instant getResolvidaEm() { return resolvidaEm; }
    public void setResolvidaEm(Instant resolvidaEm) { this.resolvidaEm = resolvidaEm; }
    public String getNotaResolucao() { return notaResolucao; }
    public void setNotaResolucao(String notaResolucao) { this.notaResolucao = notaResolucao; }
}