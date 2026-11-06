import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../config';

export type Perfil = 'GESTOR' | 'INSTRUTOR' | 'SERVICO';

export interface Sessao {
  token: string;
  perfil: Perfil;
  nome: string;
  expiraEm: string;
}

const CHAVE = 'frota.sessao';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private sessaoAtual = signal<Sessao | null>(this.lerSessao());
  readonly sessao = this.sessaoAtual.asReadonly();

  login(email: string, senha: string): Observable<Sessao> {
    return this.http.post<Sessao>(`${API_URL}/auth/login`, { email, senha }).pipe(
      tap((s) => {
        localStorage.setItem(CHAVE, JSON.stringify(s));
        this.sessaoAtual.set(s);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(CHAVE);
    this.sessaoAtual.set(null);
    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return this.sessaoValida()?.token ?? null;
  }

  estaLogado(): boolean {
    return this.sessaoValida() !== null;
  }

  private sessaoValida(): Sessao | null {
    const s = this.sessaoAtual();
    if (!s) return null;
    // a API manda a data com mais de 3 casas decimais; corta para o JS entender em qualquer navegador
    const expira = Date.parse(s.expiraEm.replace(/(\.\d{3})\d+/, '$1'));
    if (Number.isNaN(expira) || expira <= Date.now()) {
      localStorage.removeItem(CHAVE);
      this.sessaoAtual.set(null);
      return null;
    }
    return s;
  }

  private lerSessao(): Sessao | null {
    try {
      const bruto = localStorage.getItem(CHAVE);
      return bruto ? (JSON.parse(bruto) as Sessao) : null;
    } catch {
      return null;
    }
  }
}