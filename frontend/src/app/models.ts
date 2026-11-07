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

export interface ChecklistResponse {
  id: number;
  veiculoId: number;
  placa: string;
  instrutor: string;
  dataHora: string;
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
  resolvidaEm: string | null;
  notaResolucao: string | null;
}

export interface ParametroResponse {
  id: number;
  nome: string;
  tipoVeiculo: TipoVeiculo;
  intervaloKm: number | null;
  intervaloDias: number | null;
}

export interface ManutencaoRequest {
  veiculoId: number;
  parametroId: number;
  data: string;
  kmNaData: number;
  custo: number | null;
  observacao: string | null;
}

export interface ManutencaoResponse {
  id: number;
  veiculoId: number;
  placa: string;
  parametroId: number;
  tipoManutencao: string;
  data: string;
  kmNaData: number;
  custo: number | null;
  observacao: string | null;
}

export interface HistoricoVeiculo {
  veiculoId: number;
  placa: string;
  modelo: string;
  tipo: TipoVeiculo;
  kmAtual: number;
  status: StatusSemaforo;
  pendencias: Pendencia[];
  manutencoes: ManutencaoResponse[];
  checklistsRecentes: ChecklistResponse[];
  anomalias: AnomaliaResponse[];
}

export interface VeiculoRequest {
  placa: string;
  modelo: string;
  ano: number;
  tipo: TipoVeiculo;
  kmAtual: number;
  vencimentoIpva: string | null;
  vencimentoLicenciamento: string | null;
}

export interface VeiculoResponse extends VeiculoRequest {
  id: number;
  ativo: boolean;
}