import { StatusSemaforo } from '../models';

export const COR_FUNDO: Record<StatusSemaforo, string> = {
  VERDE: 'bg-green-500',
  AMARELO: 'bg-yellow-400',
  VERMELHO: 'bg-red-500',
};

export const ROTULO_STATUS: Record<StatusSemaforo, string> = {
  VERDE: 'Em dia',
  AMARELO: 'Atenção',
  VERMELHO: 'Urgente',
};