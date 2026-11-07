import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE, API_URL } from '../config';
import {
  AnomaliaRequest,
  AnomaliaResponse,
  ChecklistRequest,
  HistoricoVeiculo,
  ManutencaoRequest,
  ManutencaoResponse,
  ParametroResponse,
  SemaforoVeiculo,
  TipoVeiculo,
  VeiculoRequest,
  VeiculoResponse,
} from '../models';

@Injectable({ providedIn: 'root' })
export class FrotaApi {
  private http = inject(HttpClient);

  semaforo() {
    return this.http.get<SemaforoVeiculo[]>(`${API_URL}/semaforo`);
  }

  semaforoDoVeiculo(id: number) {
    return this.http.get<SemaforoVeiculo>(`${API_URL}/semaforo/${id}`);
  }

  historico(id: number) {
    return this.http.get<HistoricoVeiculo>(`${API_URL}/veiculos/${id}/historico`);
  }

  veiculo(id: number) {
    return this.http.get<VeiculoResponse>(`${API_URL}/veiculos/${id}`);
  }

  criarVeiculo(req: VeiculoRequest) {
    return this.http.post<VeiculoResponse>(`${API_URL}/veiculos`, req);
  }

  atualizarVeiculo(id: number, req: VeiculoRequest) {
    return this.http.put<VeiculoResponse>(`${API_URL}/veiculos/${id}`, req);
  }

  desativarVeiculo(id: number) {
    return this.http.delete<void>(`${API_URL}/veiculos/${id}`);
  }

  parametros(tipo: TipoVeiculo) {
    return this.http.get<ParametroResponse[]>(`${API_URL}/parametros`, { params: { tipo } });
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

  resolverAnomalia(id: number, nota: string | null) {
    return this.http.put<AnomaliaResponse>(`${API_URL}/anomalias/${id}/resolver`, { nota });
  }

  // a foto exige o token, então é baixada pelo código e exibida a partir do arquivo em memória
  foto(fotoUrl: string) {
    return this.http.get(`${API_BASE}${fotoUrl}`, { responseType: 'blob' });
  }

  registrarManutencao(req: ManutencaoRequest) {
    return this.http.post<ManutencaoResponse>(`${API_URL}/manutencoes`, req);
  }
}