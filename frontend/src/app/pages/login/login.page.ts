import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  template: `
    <main class="min-h-screen flex items-center justify-center bg-slate-100 p-4">
      <form (ngSubmit)="entrar()" class="w-full max-w-sm bg-white rounded-2xl shadow p-6 space-y-4">
        <div class="text-center">
          <h1 class="text-2xl font-bold text-slate-800">Frota Colibri</h1>
          <p class="text-sm text-slate-500">Entre para continuar</p>
        </div>

        <label class="block">
          <span class="text-sm font-medium text-slate-700">E-mail</span>
          <input
            type="email"
            name="email"
            autocomplete="username"
            required
            [ngModel]="email()"
            (ngModelChange)="email.set($event)"
            class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </label>

        <label class="block">
          <span class="text-sm font-medium text-slate-700">Senha</span>
          <input
            type="password"
            name="senha"
            autocomplete="current-password"
            required
            [ngModel]="senha()"
            (ngModelChange)="senha.set($event)"
            class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </label>

        @if (erro(); as msg) {
          <p class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">{{ msg }}</p>
        }

        <button
          type="submit"
          [disabled]="carregando()"
          class="w-full h-12 rounded-lg bg-blue-600 text-white font-semibold disabled:opacity-60"
        >
          {{ carregando() ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </main>
  `,
})
export class LoginPage {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = signal('');
  senha = signal('');
  carregando = signal(false);
  erro = signal<string | null>(null);

  entrar(): void {
    if (this.carregando()) return;
    this.erro.set(null);
    this.carregando.set(true);
    this.auth.login(this.email().trim(), this.senha()).subscribe({
      next: () =>
        this.router.navigate([this.auth.sessao()?.perfil === 'INSTRUTOR' ? '/checklist' : '/']),
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erro.set(this.mensagem(e));
      },
    });
  }

  private mensagem(e: HttpErrorResponse): string {
    if (e.status === 0) return 'Não foi possível conectar ao servidor. Verifique a conexão.';
    return e.error?.erro ?? 'Não foi possível entrar. Tente novamente.';
  }
}