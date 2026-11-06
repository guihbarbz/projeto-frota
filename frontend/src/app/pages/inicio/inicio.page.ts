import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API_URL } from '../../config';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  template: `
    <main class="min-h-screen bg-slate-100 p-4">
      <div class="mx-auto max-w-md bg-white rounded-2xl shadow p-6 space-y-4">
        <h1 class="text-xl font-bold text-slate-800">Olá, {{ sessao()?.nome }}</h1>
        <p class="text-slate-600">Perfil: {{ sessao()?.perfil }}</p>

        @if (total() !== null) {
          <p class="text-slate-600">{{ total() }} veículo(s) monitorado(s)</p>
        }

        <a
          routerLink="/checklist"
          class="flex h-12 items-center justify-center rounded-lg bg-blue-600 text-white font-semibold"
        >
          Abrir checklist
        </a>

        <button
          type="button"
          (click)="sair()"
          class="w-full h-12 rounded-lg border border-slate-300 text-slate-700 font-medium"
        >
          Sair
        </button>
      </div>
    </main>
  `,
})
export class InicioPage {
  private auth = inject(AuthService);
  private http = inject(HttpClient);

  sessao = this.auth.sessao;
  total = signal<number | null>(null);

  constructor() {
    this.http.get<unknown[]>(`${API_URL}/semaforo`).subscribe({
      next: (v) => this.total.set(v.length),
      error: () => this.total.set(null),
    });
  }

  sair(): void {
    this.auth.logout();
  }
}