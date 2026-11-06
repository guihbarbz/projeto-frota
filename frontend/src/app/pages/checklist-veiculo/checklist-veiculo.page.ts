import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { firstValueFrom, forkJoin } from 'rxjs';
import { FrotaApi } from '../../api/frota.api';
import { AnomaliaResponse, Gravidade, SemaforoVeiculo } from '../../models';
import { mensagemDeErro } from '../../util/erro';
import { reduzirImagem } from '../../util/imagem';
import { COR_FUNDO, ROTULO_STATUS } from '../../util/status';

@Component({
  selector: 'app-checklist-veiculo',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="min-h-screen bg-slate-100 pb-10">
      <header class="bg-white shadow-sm px-4 py-3 flex items-center gap-3">
        <a
          routerLink="/checklist"
          aria-label="Voltar"
          class="h-10 w-10 flex items-center justify-center rounded-lg border border-slate-300 text-slate-700"
          >←</a
        >
        <h1 class="text-lg font-bold text-slate-800">Checklist</h1>
      </header>

      <section class="p-4 space-y-4 max-w-md mx-auto">
        @if (carregando()) {
          <p class="text-slate-500">Carregando...</p>
        }

        @if (erroCarga(); as msg) {
          <div class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">
            {{ msg }}
            <button type="button" (click)="carregar()" class="underline ml-1">Tentar de novo</button>
          </div>
        }

        @if (veiculo(); as v) {
          <!-- situação do veículo -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-2">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-xl font-bold text-slate-800">{{ v.placa }}</p>
                <p class="text-sm text-slate-600">{{ v.modelo }}</p>
              </div>
              <span class="flex items-center gap-2 text-sm font-medium text-slate-700">
                <span class="h-3 w-3 rounded-full" [class]="corFundo[v.status]"></span>
                {{ rotulo[v.status] }}
              </span>
            </div>
            @for (p of v.pendencias; track $index) {
              <p
                class="text-sm rounded-lg px-3 py-2"
                [class]="p.status === 'VERMELHO' ? 'bg-red-50 text-red-700' : 'bg-yellow-50 text-yellow-800'"
              >
                {{ p.motivo }}
              </p>
            }
          </div>

          <!-- problemas já reportados -->
          @if (abertas().length > 0) {
            <div class="bg-white rounded-2xl shadow p-4 space-y-2">
              <h2 class="font-semibold text-slate-800">Problemas já reportados</h2>
              @for (a of abertas(); track a.id) {
                <p class="text-sm text-slate-700">
                  • {{ a.descricao }} <span class="text-slate-400">({{ a.reportadoPor }})</span>
                </p>
              }
            </div>
          }

          <!-- checklist -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-3">
            <h2 class="font-semibold text-slate-800">Km e observação</h2>

            @if (checklistFeito()) {
              <p class="rounded-lg bg-green-50 text-green-800 text-sm p-3" role="status">
                Checklist registrado. Bom trabalho!
              </p>
            } @else {
              <label class="block">
                <span class="text-sm font-medium text-slate-700">Km atual do veículo</span>
                <input
                  type="number"
                  inputmode="numeric"
                  [ngModel]="km()"
                  (ngModelChange)="km.set($event)"
                  class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </label>

              <label class="block">
                <span class="text-sm font-medium text-slate-700">Observação (opcional)</span>
                <textarea
                  rows="2"
                  maxlength="500"
                  [ngModel]="observacao()"
                  (ngModelChange)="observacao.set($event)"
                  class="mt-1 w-full rounded-lg border border-slate-300 p-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
                ></textarea>
              </label>

              @if (erroChecklist(); as msg) {
                <p class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">{{ msg }}</p>
              }

              <button
                type="button"
                (click)="registrarChecklist()"
                [disabled]="enviandoChecklist()"
                class="w-full h-12 rounded-lg bg-green-600 text-white font-semibold disabled:opacity-60"
              >
                {{ enviandoChecklist() ? 'Registrando...' : 'Registrar checklist' }}
              </button>
            }
          </div>

          <!-- reportar problema -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-3">
            @if (problemaRegistrado()) {
              <p class="rounded-lg bg-green-50 text-green-800 text-sm p-3" role="status">
                Problema registrado.
              </p>
              <button
                type="button"
                (click)="novoProblema()"
                class="w-full h-12 rounded-lg border border-slate-300 text-slate-700 font-medium"
              >
                Reportar outro problema
              </button>
            } @else if (!mostrandoProblema()) {
              <button
                type="button"
                (click)="mostrandoProblema.set(true)"
                class="w-full h-12 rounded-lg border border-red-300 text-red-700 font-medium"
              >
                Reportar um problema
              </button>
            } @else {
              <h2 class="font-semibold text-slate-800">Reportar problema</h2>

              <label class="block">
                <span class="text-sm font-medium text-slate-700">O que aconteceu?</span>
                <textarea
                  rows="3"
                  maxlength="500"
                  [ngModel]="descricao()"
                  (ngModelChange)="descricao.set($event)"
                  class="mt-1 w-full rounded-lg border border-slate-300 p-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
                ></textarea>
              </label>

              <div>
                <span class="text-sm font-medium text-slate-700">Gravidade</span>
                <div class="mt-1 grid grid-cols-3 gap-2">
                  @for (g of opcoesGravidade; track g.valor) {
                    <button
                      type="button"
                      (click)="gravidade.set(g.valor)"
                      class="h-12 rounded-lg text-sm font-medium"
                      [class]="
                        gravidade() === g.valor
                          ? 'bg-blue-600 text-white'
                          : 'bg-white text-slate-700 border border-slate-300'
                      "
                    >
                      {{ g.rotulo }}
                    </button>
                  }
                </div>
                <p class="mt-1 text-xs text-slate-500">Grave: o veículo não deve rodar até resolver.</p>
              </div>

              <div>
                <label
                  class="flex h-12 items-center justify-center rounded-lg border border-dashed border-slate-400 text-slate-700 text-sm font-medium"
                >
                  <input type="file" accept="image/*" class="hidden" (change)="onFoto($event)" />
                  {{ foto() ? 'Trocar foto' : 'Adicionar foto (opcional)' }}
                </label>
                @if (foto(); as f) {
                  <p class="mt-1 text-xs text-slate-500">{{ f.name }}</p>
                }
              </div>

              @if (erroProblema(); as msg) {
                <p class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">{{ msg }}</p>
              }

              <div class="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  (click)="cancelarProblema()"
                  class="h-12 rounded-lg border border-slate-300 text-slate-700 font-medium"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  (click)="enviarProblema()"
                  [disabled]="enviandoProblema()"
                  class="h-12 rounded-lg bg-red-600 text-white font-semibold disabled:opacity-60"
                >
                  {{ enviandoProblema() ? 'Enviando...' : 'Enviar' }}
                </button>
              </div>
            }
          </div>
        }
      </section>
    </main>
  `,
})
export class ChecklistVeiculoPage {
  private api = inject(FrotaApi);
  private id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  corFundo = COR_FUNDO;
  rotulo = ROTULO_STATUS;
  opcoesGravidade: { valor: Gravidade; rotulo: string }[] = [
    { valor: 'BAIXA', rotulo: 'Pequeno' },
    { valor: 'MEDIA', rotulo: 'Médio' },
    { valor: 'ALTA', rotulo: 'Grave' },
  ];

  veiculo = signal<SemaforoVeiculo | null>(null);
  abertas = signal<AnomaliaResponse[]>([]);
  carregando = signal(true);
  erroCarga = signal<string | null>(null);

  km = signal<number | null>(null);
  observacao = signal('');
  enviandoChecklist = signal(false);
  erroChecklist = signal<string | null>(null);
  checklistFeito = signal(false);

  mostrandoProblema = signal(false);
  descricao = signal('');
  gravidade = signal<Gravidade | null>(null);
  foto = signal<File | null>(null);
  enviandoProblema = signal(false);
  erroProblema = signal<string | null>(null);
  anomaliaCriadaId = signal<number | null>(null);
  problemaRegistrado = signal(false);

  constructor() {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erroCarga.set(null);
    forkJoin({
      veiculo: this.api.semaforoDoVeiculo(this.id),
      abertas: this.api.anomaliasAbertas(this.id),
    }).subscribe({
      next: ({ veiculo, abertas }) => {
        this.veiculo.set(veiculo);
        this.abertas.set(abertas);
        this.km.set(veiculo.kmAtual);
        this.carregando.set(false);
      },
      error: (e) => {
        this.erroCarga.set(mensagemDeErro(e, 'Não foi possível carregar o veículo.'));
        this.carregando.set(false);
      },
    });
  }

  registrarChecklist(): void {
    const v = this.veiculo();
    const km = this.km();
    if (!v) return;
    if (km === null || km < v.kmAtual) {
      this.erroChecklist.set(`Informe o km atual do veículo (mínimo ${v.kmAtual}).`);
      return;
    }
    this.erroChecklist.set(null);
    this.enviandoChecklist.set(true);
    this.api
      .registrarChecklist({
        veiculoId: v.veiculoId,
        km,
        observacao: this.observacao().trim() || null,
      })
      .subscribe({
        next: () => {
          this.checklistFeito.set(true);
          this.enviandoChecklist.set(false);
        },
        error: (e) => {
          this.erroChecklist.set(mensagemDeErro(e, 'Não foi possível registrar o checklist.'));
          this.enviandoChecklist.set(false);
        },
      });
  }

  onFoto(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.foto.set(input.files?.[0] ?? null);
  }

  async enviarProblema(): Promise<void> {
    const v = this.veiculo();
    const gravidade = this.gravidade();
    const descricao = this.descricao().trim();
    if (!v) return;
    if (!descricao || !gravidade) {
      this.erroProblema.set('Descreva o problema e escolha a gravidade.');
      return;
    }
    this.erroProblema.set(null);
    this.enviandoProblema.set(true);
    try {
      let anomaliaId = this.anomaliaCriadaId();
      if (anomaliaId === null) {
        const criada = await firstValueFrom(
          this.api.reportarAnomalia({ veiculoId: v.veiculoId, descricao, gravidade }),
        );
        anomaliaId = criada.id;
        this.anomaliaCriadaId.set(anomaliaId);
      }
      const foto = this.foto();
      if (foto) {
        const reduzida = await reduzirImagem(foto);
        await firstValueFrom(this.api.enviarFoto(anomaliaId, reduzida));
      }
      this.problemaRegistrado.set(true);
      this.mostrandoProblema.set(false);
      this.atualizarSituacao();
    } catch (e) {
      this.erroProblema.set(
        this.anomaliaCriadaId() !== null
          ? 'O problema foi registrado, mas a foto não foi enviada (' +
              mensagemDeErro(e, 'erro desconhecido') +
              '). Toque em Enviar para tentar a foto de novo.'
          : mensagemDeErro(e, 'Não foi possível registrar o problema.'),
      );
    } finally {
      this.enviandoProblema.set(false);
    }
  }

  cancelarProblema(): void {
    if (this.anomaliaCriadaId() !== null) this.atualizarSituacao();
    this.limparProblema();
    this.mostrandoProblema.set(false);
  }

  novoProblema(): void {
    this.limparProblema();
    this.problemaRegistrado.set(false);
    this.mostrandoProblema.set(true);
  }

  private limparProblema(): void {
    this.descricao.set('');
    this.gravidade.set(null);
    this.foto.set(null);
    this.erroProblema.set(null);
    this.anomaliaCriadaId.set(null);
  }

  // depois de reportar, busca de novo a cor do veículo e a lista de problemas abertos
  private atualizarSituacao(): void {
    forkJoin({
      veiculo: this.api.semaforoDoVeiculo(this.id),
      abertas: this.api.anomaliasAbertas(this.id),
    }).subscribe({
      next: ({ veiculo, abertas }) => {
        this.veiculo.set(veiculo);
        this.abertas.set(abertas);
      },
    });
  }
}