export type StatusSemaforo = 'VERDE' | 'AMARELO' | 'VERMELHO';
export type TipoVeiculo = 'CARRO' | 'MOTO';
export type Gravidade = 'BAIXA' | 'MEDIA' | 'ALTA';

export interface Pendencia {
  tipo: string;
  status: StatusSemaforo;
  motivo: string;
  kmRestantes: number | null;
  diasRestantes: number | null;
}

export interface SemaforoVeiculo {
  veiculoId: number;
  placa: string;
  modelo: string;
  tipo: TipoVeiculo;
  kmAtual: number;
  status: StatusSemaforo;
  pendencias: Pendencia[];
}

export interface ChecklistRequest {
  veiculoId: number;
  km: number;
  observacao: string | null;
}

export interface AnomaliaRequest {
  veiculoId: number;
  descricao: string;
  gravidade: Gravidade;
}

export interface AnomaliaResponse {
  id: number;
  veiculoId: number;
  placa: string;
  reportadoPor: string;
  descricao: string;
  gravidade: Gravidade;
  status: 'ABERTA' | 'RESOLVIDA';
  fotoUrl: string | null;
  criadaEm: string;
}