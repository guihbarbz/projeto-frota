import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { FrotaApi } from '../../api/frota.api';
import { SemaforoVeiculo } from '../../models';
import { mensagemDeErro } from '../../util/erro';
import { COR_FUNDO, ROTULO_STATUS } from '../../util/status';

@Component({
  selector: 'app-painel',
  imports: [RouterLink],
  template: `
    <main class="min-h-screen bg-slate-100 pb-10">
      <header class="bg-white shadow-sm px-4 py-3 flex items-center justify-between">
        <div>
          <h1 class="text-lg font-bold text-slate-800">Painel da frota</h1>
          <p class="text-xs text-slate-500">{{ sessao()?.nome }}</p>
        </div>
        <div class="flex gap-2">
          <a
            routerLink="/painel/veiculos/novo"
            class="h-10 px-3 flex items-center rounded-lg bg-blue-600 text-sm font-medium text-white"
            >Novo veículo</a
          >
          <a
            routerLink="/checklist"
            class="h-10 px-3 flex items-center rounded-lg border border-slate-300 text-sm text-slate-700"
            >Checklist</a
          >
          <button
            type="button"
            (click)="sair()"
            class="h-10 px-3 rounded-lg border border-slate-300 text-sm text-slate-700"
          >
            Sair
          </button>
        </div>
      </header>

      <section class="p-4 space-y-4 max-w-3xl mx-auto">
        <div class="grid grid-cols-3 gap-2 text-center">
          <div class="rounded-2xl bg-white shadow p-3">
            <p class="text-3xl font-bold text-red-600">{{ vermelhos() }}</p>
            <p class="text-xs text-slate-600">Urgente</p>
          </div>
          <div class="rounded-2xl bg-white shadow p-3">
            <p class="text-3xl font-bold text-yellow-600">{{ amarelos() }}</p>
            <p class="text-xs text-slate-600">Atenção</p>
          </div>
          <div class="rounded-2xl bg-white shadow p-3">
            <p class="text-3xl font-bold text-green-600">{{ verdes() }}</p>
            <p class="text-xs text-slate-600">Em dia</p>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <h2 class="font-semibold text-slate-800">Veículos</h2>
          <button type="button" (click)="carregar()" class="text-sm text-blue-700 underline">
            Atualizar
          </button>
        </div>

        @if (carregando()) {
          <p class="text-slate-500">Carregando...</p>
        }

        @if (erro(); as msg) {
          <div class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">
            {{ msg }}
            <button type="button" (click)="carregar()" class="underline ml-1">Tentar de novo</button>
          </div>
        }

        <div class="grid gap-3 sm:grid-cols-2">
          @for (v of veiculos(); track v.veiculoId) {
            <a
              [routerLink]="['/painel/veiculo', v.veiculoId]"
              class="block bg-white rounded-2xl shadow p-4 active:bg-slate-50"
            >
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-lg font-bold text-slate-800">{{ v.placa }}</p>
                  <p class="text-sm text-slate-600">
                    {{ v.modelo }} · {{ v.tipo === 'MOTO' ? 'Moto' : 'Carro' }} ·
                    {{ v.kmAtual }} km
                  </p>
                </div>
                <span class="flex items-center gap-2 text-sm font-medium text-slate-700">
                  <span class="h-3 w-3 rounded-full" [class]="corFundo[v.status]"></span>
                  {{ rotulo[v.status] }}
                </span>
              </div>
              @for (p of v.pendencias.slice(0, 2); track $index) {
                <p class="mt-2 text-sm text-slate-600">{{ p.motivo }}</p>
              }
              @if (v.pendencias.length > 2) {
                <p class="mt-1 text-xs text-slate-400">+ {{ v.pendencias.length - 2 }} pendência(s)</p>
              }
            </a>
          }
        </div>

        @if (!carregando() && !erro() && veiculos().length === 0) {
          <p class="text-slate-500">Nenhum veículo cadastrado ainda.</p>
        }
      </section>
    </main>
  `,
})
export class PainelPage {
  private api = inject(FrotaApi);
  private auth = inject(AuthService);

  sessao = this.auth.sessao;
  veiculos = signal<SemaforoVeiculo[]>([]);
  carregando = signal(true);
  erro = signal<string | null>(null);

  vermelhos = computed(() => this.veiculos().filter((v) => v.status === 'VERMELHO').length);
  amarelos = computed(() => this.veiculos().filter((v) => v.status === 'AMARELO').length);
  verdes = computed(() => this.veiculos().filter((v) => v.status === 'VERDE').length);

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
        this.erro.set(mensagemDeErro(e, 'Não foi possível carregar a frota.'));
        this.carregando.set(false);
      },
    });
  }

  sair(): void {
    this.auth.logout();
  }
}