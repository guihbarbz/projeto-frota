import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FrotaApi } from '../../api/frota.api';
import { AnomaliaResponse, HistoricoVeiculo, ParametroResponse } from '../../models';
import { dataBrParaIso, hojeLocal, isoParaDataBr, mascararData } from '../../util/data';
import { mensagemDeErro } from '../../util/erro';
import { COR_FUNDO, ROTULO_GRAVIDADE, ROTULO_STATUS } from '../../util/status';

@Component({
  selector: 'app-veiculo-detalhe',
  imports: [FormsModule, RouterLink, DatePipe, CurrencyPipe],
  template: `
    <main class="min-h-screen bg-slate-100 pb-10">
      <header class="bg-white shadow-sm px-4 py-3 flex items-center gap-3">
        <a
          routerLink="/"
          aria-label="Voltar"
          class="h-10 w-10 flex items-center justify-center rounded-lg border border-slate-300 text-slate-700"
          >←</a
        >
        <h1 class="flex-1 text-lg font-bold text-slate-800">Veículo</h1>
        @if (h(); as v) {
          <a
            [routerLink]="['/painel/veiculos', v.veiculoId, 'editar']"
            class="h-10 px-3 flex items-center rounded-lg border border-slate-300 text-sm text-slate-700"
            >Editar</a
          >
        }
      </header>

      <section class="p-4 space-y-4 max-w-2xl mx-auto">
        @if (carregando()) {
          <p class="text-slate-500">Carregando...</p>
        }

        @if (erroGeral(); as msg) {
          <div class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">{{ msg }}</div>
        }

        @if (h(); as v) {
          <!-- situação -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-2">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-xl font-bold text-slate-800">{{ v.placa }}</p>
                <p class="text-sm text-slate-600">
                  {{ v.modelo }} · {{ v.tipo === 'MOTO' ? 'Moto' : 'Carro' }} · {{ v.kmAtual }} km
                </p>
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
            @if (v.pendencias.length === 0) {
              <p class="text-sm text-green-700">Nenhuma pendência.</p>
            }
          </div>

          <!-- problemas -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-3">
            <h2 class="font-semibold text-slate-800">Problemas reportados</h2>
            @if (v.anomalias.length === 0) {
              <p class="text-sm text-slate-500">Nenhum problema reportado.</p>
            }
            @for (a of v.anomalias; track a.id) {
              <div
                class="rounded-lg border p-3 space-y-2"
                [class]="a.status === 'ABERTA' ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-slate-50'"
              >
                <p class="text-sm text-slate-800">{{ a.descricao }}</p>
                <p class="text-xs text-slate-500">
                  {{ gravidade[a.gravidade] }} · {{ a.reportadoPor }} ·
                  {{ a.criadaEm | date: 'dd/MM HH:mm' }}
                </p>
                @if (a.status === 'RESOLVIDA') {
                  <p class="text-xs text-green-700">
                    Resolvida{{ a.notaResolucao ? ': ' + a.notaResolucao : '' }}
                  </p>
                }

                <div class="flex gap-2">
                  @if (a.fotoUrl) {
                    <button
                      type="button"
                      (click)="verFoto(a)"
                      class="h-10 px-3 rounded-lg border border-slate-300 bg-white text-sm text-slate-700"
                    >
                      Ver foto
                    </button>
                  }
                  @if (a.status === 'ABERTA' && resolvendoId() !== a.id) {
                    <button
                      type="button"
                      (click)="iniciarResolucao(a.id)"
                      class="h-10 px-3 rounded-lg bg-green-600 text-sm font-medium text-white"
                    >
                      Resolver
                    </button>
                  }
                </div>

                @if (resolvendoId() === a.id) {
                  <textarea
                    rows="2"
                    maxlength="500"
                    placeholder="Nota (opcional): o que foi feito?"
                    [ngModel]="nota()"
                    (ngModelChange)="nota.set($event)"
                    class="w-full rounded-lg border border-slate-300 p-3 text-base"
                  ></textarea>
                  <div class="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      (click)="resolvendoId.set(null)"
                      class="h-10 rounded-lg border border-slate-300 bg-white text-sm text-slate-700"
                    >
                      Cancelar
                    </button>
                    <button
                      type="button"
                      (click)="confirmarResolucao(a.id)"
                      class="h-10 rounded-lg bg-green-600 text-sm font-medium text-white"
                    >
                      Confirmar
                    </button>
                  </div>
                }
              </div>
            }
          </div>

          <!-- registrar manutenção -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-3">
            <h2 class="font-semibold text-slate-800">Registrar manutenção</h2>

            @if (manutencaoOk()) {
              <p class="rounded-lg bg-green-50 text-green-800 text-sm p-3" role="status">
                Manutenção registrada.
              </p>
            }

            <label class="block">
              <span class="text-sm font-medium text-slate-700">Tipo</span>
              <select
                [ngModel]="parametroId()"
                (ngModelChange)="parametroId.set($event)"
                class="mt-1 w-full h-12 rounded-lg border border-slate-300 bg-white px-3 text-base"
              >
                <option [ngValue]="null">Escolha o tipo de manutenção</option>
                @for (p of parametros(); track p.id) {
                  <option [ngValue]="p.id">{{ p.nome }}</option>
                }
              </select>
            </label>

            <div class="grid grid-cols-2 gap-3">
              <label class="block">
                <span class="text-sm font-medium text-slate-700">Data</span>
                <input
                  type="text"
                  inputmode="numeric"
                  maxlength="10"
                  autocomplete="off"
                  placeholder="dd/mm/aaaa"
                  [value]="dataManut()"
                  (input)="onData($event)"
                  class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base"
                />
              </label>
              <label class="block">
                <span class="text-sm font-medium text-slate-700">Km na data</span>
                <input
                  type="number"
                  inputmode="numeric"
                  [ngModel]="kmManut()"
                  (ngModelChange)="kmManut.set($event)"
                  class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base"
                />
              </label>
            </div>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">Custo em R$ (opcional)</span>
              <input
                type="number"
                inputmode="decimal"
                step="0.01"
                [ngModel]="custo()"
                (ngModelChange)="custo.set($event)"
                class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base"
              />
            </label>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">Observação (opcional)</span>
              <textarea
                rows="2"
                maxlength="500"
                [ngModel]="obsManut()"
                (ngModelChange)="obsManut.set($event)"
                class="mt-1 w-full rounded-lg border border-slate-300 p-3 text-base"
              ></textarea>
            </label>

            @if (erroManutencao(); as msg) {
              <p class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">{{ msg }}</p>
            }

            <button
              type="button"
              (click)="registrarManutencao()"
              [disabled]="enviandoManutencao()"
              class="w-full h-12 rounded-lg bg-blue-600 text-white font-semibold disabled:opacity-60"
            >
              {{ enviandoManutencao() ? 'Registrando...' : 'Registrar manutenção' }}
            </button>
          </div>

          <!-- histórico de manutenções -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-2">
            <h2 class="font-semibold text-slate-800">Manutenções</h2>
            @if (v.manutencoes.length === 0) {
              <p class="text-sm text-slate-500">Nenhuma manutenção registrada.</p>
            }
            @for (m of v.manutencoes; track m.id) {
              <div class="border-b border-slate-100 pb-2 last:border-0">
                <p class="text-sm font-medium text-slate-800">{{ m.tipoManutencao }}</p>
                <p class="text-xs text-slate-500">
                  {{ m.data | date: 'dd/MM/yyyy' }} · {{ m.kmNaData }} km
                  @if (m.custo !== null) {
                    · {{ m.custo | currency: 'BRL' }}
                  }
                </p>
                @if (m.observacao) {
                  <p class="text-xs text-slate-600">{{ m.observacao }}</p>
                }
              </div>
            }
          </div>

          <!-- checklists recentes -->
          <div class="bg-white rounded-2xl shadow p-4 space-y-2">
            <h2 class="font-semibold text-slate-800">Checklists recentes</h2>
            @if (v.checklistsRecentes.length === 0) {
              <p class="text-sm text-slate-500">Nenhum checklist registrado.</p>
            }
            @for (c of v.checklistsRecentes; track c.id) {
              <div class="border-b border-slate-100 pb-2 last:border-0">
                <p class="text-sm text-slate-800">{{ c.km }} km · {{ c.instrutor }}</p>
                <p class="text-xs text-slate-500">{{ c.dataHora | date: 'dd/MM/yyyy HH:mm' }}</p>
                @if (c.observacao) {
                  <p class="text-xs text-slate-600">{{ c.observacao }}</p>
                }
              </div>
            }
          </div>
        }
      </section>

      @if (foto(); as f) {
        <button
          type="button"
          (click)="fecharFoto()"
          aria-label="Fechar foto"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4"
        >
          <img [src]="f" alt="Foto do problema" class="max-h-full max-w-full rounded-lg" />
        </button>
      }
    </main>
  `,
})
export class VeiculoDetalhePage {
  private api = inject(FrotaApi);
  private id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  corFundo = COR_FUNDO;
  rotulo = ROTULO_STATUS;
  gravidade = ROTULO_GRAVIDADE;

  h = signal<HistoricoVeiculo | null>(null);
  parametros = signal<ParametroResponse[]>([]);
  carregando = signal(true);
  erroGeral = signal<string | null>(null);

  resolvendoId = signal<number | null>(null);
  nota = signal('');
  foto = signal<string | null>(null);

  parametroId = signal<number | null>(null);
  dataManut = signal(isoParaDataBr(hojeLocal()));
  kmManut = signal<number | null>(null);
  custo = signal<number | null>(null);
  obsManut = signal('');
  enviandoManutencao = signal(false);
  erroManutencao = signal<string | null>(null);
  manutencaoOk = signal(false);

  constructor() {
    this.api.historico(this.id).subscribe({
      next: (h) => {
        this.h.set(h);
        this.kmManut.set(h.kmAtual);
        this.carregando.set(false);
        this.api.parametros(h.tipo).subscribe({
          next: (p) => this.parametros.set(p),
          error: (e) =>
            this.erroGeral.set(mensagemDeErro(e, 'Não foi possível carregar os tipos de manutenção.')),
        });
      },
      error: (e) => {
        this.erroGeral.set(mensagemDeErro(e, 'Não foi possível carregar o veículo.'));
        this.carregando.set(false);
      },
    });
  }

  private recarregar(): void {
    this.api.historico(this.id).subscribe({
      next: (h) => {
        this.h.set(h);
        this.kmManut.set(h.kmAtual);
      },
      error: (e) => this.erroGeral.set(mensagemDeErro(e, 'Não foi possível atualizar os dados.')),
    });
  }

  iniciarResolucao(id: number): void {
    this.nota.set('');
    this.resolvendoId.set(id);
  }

  confirmarResolucao(id: number): void {
    this.erroGeral.set(null);
    this.api.resolverAnomalia(id, this.nota().trim() || null).subscribe({
      next: () => {
        this.resolvendoId.set(null);
        this.nota.set('');
        this.recarregar();
      },
      error: (e) => this.erroGeral.set(mensagemDeErro(e, 'Não foi possível resolver o problema.')),
    });
  }

  verFoto(a: AnomaliaResponse): void {
    if (!a.fotoUrl) return;
    this.api.foto(a.fotoUrl).subscribe({
      next: (blob) => this.foto.set(URL.createObjectURL(blob)),
      error: (e) => this.erroGeral.set(mensagemDeErro(e, 'Não foi possível abrir a foto.')),
    });
  }

  fecharFoto(): void {
    const f = this.foto();
    if (f) URL.revokeObjectURL(f);
    this.foto.set(null);
  }

  onData(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const mascarado = mascararData(campo.value);
    campo.value = mascarado;
    this.dataManut.set(mascarado);
  }

  registrarManutencao(): void {
    const parametroId = this.parametroId();
    const km = this.kmManut();
    if (parametroId === null || km === null) {
      this.erroManutencao.set('Escolha o tipo e preencha o km.');
      return;
    }
    const data = dataBrParaIso(this.dataManut());
    if (!data) {
      this.erroManutencao.set('Data inválida. Use o formato dd/mm/aaaa.');
      return;
    }
    this.erroManutencao.set(null);
    this.manutencaoOk.set(false);
    this.enviandoManutencao.set(true);
    this.api
      .registrarManutencao({
        veiculoId: this.id,
        parametroId,
        data,
        kmNaData: km,
        custo: this.custo(),
        observacao: this.obsManut().trim() || null,
      })
      .subscribe({
        next: () => {
          this.enviandoManutencao.set(false);
          this.manutencaoOk.set(true);
          this.parametroId.set(null);
          this.custo.set(null);
          this.obsManut.set('');
          this.recarregar();
        },
        error: (e) => {
          this.enviandoManutencao.set(false);
          this.erroManutencao.set(mensagemDeErro(e, 'Não foi possível registrar a manutenção.'));
        },
      });
  }
}