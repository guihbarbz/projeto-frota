import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { FrotaApi } from '../../api/frota.api';
import { SemaforoVeiculo } from '../../models';
import { mensagemDeErro } from '../../util/erro';
import { COR_FUNDO, ROTULO_STATUS } from '../../util/status';

@Component({
  selector: 'app-checklist-lista',
  imports: [RouterLink],
  template: `
    <main class="min-h-screen bg-slate-100 pb-8">
      <header class="bg-white shadow-sm px-4 py-3 flex items-center justify-between">
        <div>
          <h1 class="text-lg font-bold text-slate-800">Checklist</h1>
          <p class="text-xs text-slate-500">{{ sessao()?.nome }}</p>
        </div>
        <button
          type="button"
          (click)="sair()"
          class="h-10 px-4 rounded-lg border border-slate-300 text-sm text-slate-700"
        >
          Sair
        </button>
      </header>

      <section class="p-4 space-y-3 max-w-md mx-auto">
        <p class="text-sm text-slate-600">Escolha o veículo:</p>

        @if (carregando()) {
          <p class="text-slate-500">Carregando...</p>
        }

        @if (erro(); as msg) {
          <div class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">
            {{ msg }}
            <button type="button" (click)="carregar()" class="underline ml-1">Tentar de novo</button>
          </div>
        }

        @for (v of veiculos(); track v.veiculoId) {
          <a
            [routerLink]="['/checklist', v.veiculoId]"
            class="block bg-white rounded-2xl shadow p-4 active:bg-slate-50"
          >
            <div class="flex items-center justify-between">
              <div>
                <p class="text-lg font-bold text-slate-800">{{ v.placa }}</p>
                <p class="text-sm text-slate-600">
                  {{ v.modelo }} · {{ v.tipo === 'MOTO' ? 'Moto' : 'Carro' }}
                </p>
              </div>
              <span class="flex items-center gap-2 text-sm font-medium text-slate-700">
                <span class="h-3 w-3 rounded-full" [class]="corFundo[v.status]"></span>
                {{ rotulo[v.status] }}
              </span>
            </div>
            @if (v.pendencias.length > 0) {
              <p class="mt-2 text-sm text-slate-600">{{ v.pendencias[0].motivo }}</p>
            }
          </a>
        }

        @if (!carregando() && !erro() && veiculos().length === 0) {
          <p class="text-slate-500">Nenhum veículo cadastrado.</p>
        }
      </section>
    </main>
  `,
})
export class ChecklistListaPage {
  private api = inject(FrotaApi);
  private auth = inject(AuthService);

  sessao = this.auth.sessao;
  veiculos = signal<SemaforoVeiculo[]>([]);
  carregando = signal(true);
  erro = signal<string | null>(null);

  corFundo = COR_FUNDO;
  rotulo = ROTULO_STATUS;

  constructor() {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.api.semaforo().subscribe({
      next: (v) => {
        this.veiculos.set(v);
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e, 'Não foi possível carregar os veículos.'));
        this.carregando.set(false);
      },
    });
  }

  sair(): void {
    this.auth.logout();
  }
}