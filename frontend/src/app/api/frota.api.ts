import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '../config';
import { AnomaliaRequest, AnomaliaResponse, ChecklistRequest, SemaforoVeiculo } from '../models';

@Injectable({ providedIn: 'root' })
export class FrotaApi {
  private http = inject(HttpClient);

  semaforo() {
    return this.http.get<SemaforoVeiculo[]>(`${API_URL}/semaforo`);
  }

  semaforoDoVeiculo(id: number) {
    return this.http.get<SemaforoVeiculo>(`${API_URL}/semaforo/${id}`);
  }

  anomaliasAbertas(veiculoId: number) {
    return this.http.get<AnomaliaResponse[]>(`${API_URL}/anomalias`, {
      params: { veiculoId, status: 'ABERTA' },
    });
  }

  registrarChecklist(req: ChecklistRequest) {
    return this.http.post(`${API_URL}/checklists`, req);
  }

  reportarAnomalia(req: AnomaliaRequest) {
    return this.http.post<AnomaliaResponse>(`${API_URL}/anomalias`, req);
  }

  enviarFoto(anomaliaId: number, arquivo: File) {
    const dados = new FormData();
    dados.append('arquivo', arquivo);
    return this.http.post<AnomaliaResponse>(`${API_URL}/anomalias/${anomaliaId}/foto`, dados);
  }
}