import { HttpErrorResponse } from '@angular/common/http';

export function mensagemDeErro(e: unknown, padrao: string): string {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) return 'Sem conexão com o servidor. Verifique a internet e tente de novo.';
    if (e.status === 403) return 'Você não tem permissão para fazer isso.';
    const msg = (e.error as { erro?: string } | null)?.erro;
    if (msg) return msg;
  }
  return padrao;
}