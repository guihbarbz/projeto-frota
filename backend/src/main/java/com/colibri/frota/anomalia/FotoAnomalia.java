package com.colibri.frota.anomalia;

import jakarta.persistence.*;

@Entity
@Table(name = "foto_anomalia")
public class FotoAnomalia {

    // mesmo id da anomalia: cada anomalia tem no máximo uma foto
    @Id
    private Long anomaliaId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] dados;

    public Long getAnomaliaId() { return anomaliaId; }
    public void setAnomaliaId(Long anomaliaId) { this.anomaliaId = anomaliaId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public byte[] getDados() { return dados; }
    public void setDados(byte[] dados) { this.dados = dados; }
}