import { Component, WritableSignal, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FrotaApi } from '../../api/frota.api';
import { TipoVeiculo, VeiculoRequest } from '../../models';
import { dataBrParaIso, isoParaDataBr, mascararData } from '../../util/data';
import { mensagemDeErro } from '../../util/erro';

@Component({
  selector: 'app-veiculo-form',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="min-h-screen bg-slate-100 pb-10">
      <header class="bg-white shadow-sm px-4 py-3 flex items-center gap-3">
        <a
          [routerLink]="id === null ? ['/'] : ['/painel/veiculo', id]"
          aria-label="Voltar"
          class="h-10 w-10 flex items-center justify-center rounded-lg border border-slate-300 text-slate-700"
          >←</a
        >
        <h1 class="text-lg font-bold text-slate-800">{{ titulo }}</h1>
      </header>

      <section class="p-4 max-w-md mx-auto">
        @if (carregando()) {
          <p class="text-slate-500">Carregando...</p>
        } @else {
          <div class="bg-white rounded-2xl shadow p-4 space-y-4">
            <label class="block">
              <span class="text-sm font-medium text-slate-700">Placa</span>
              <input
                type="text"
                maxlength="7"
                autocomplete="off"
                autocapitalize="characters"
                placeholder="ABC1D23"
                [value]="placa()"
                (input)="onPlaca($event)"
                class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base uppercase focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </label>

            <label class="block">
              <span class="text-sm font-medium text-slate-700">Modelo</span>
              <input
                type="text"
                maxlength="80"
                placeholder="Ex.: Chevrolet Onix"
                [ngModel]="modelo()"
                (ngModelChange)="modelo.set($event)"
                class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </label>

            <div>
              <span class="text-sm font-medium text-slate-700">Tipo</span>
              <div class="mt-1 grid grid-cols-2 gap-2">
                @for (t of tipos; track t.valor) {
                  <button
                    type="button"
                    (click)="tipo.set(t.valor)"
                    class="h-12 rounded-lg text-sm font-medium"
                    [class]="
                      tipo() === t.valor
                        ? 'bg-blue-600 text-white'
                        : 'bg-white text-slate-700 border border-slate-300'
                    "
                  >
                    {{ t.rotulo }}
                  </button>
                }
              </div>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <label class="block">
                <span class="text-sm font-medium text-slate-700">Ano</span>
                <input
                  type="text"
                  inputmode="numeric"
                  maxlength="4"
                  autocomplete="off"
                  placeholder="2022"
                  [value]="ano()"
                  (input)="onNumero($event, ano, 4)"
                  class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </label>
              <label class="block">
                <span class="text-sm font-medium text-slate-700">Km atual</span>
                <input
                  type="text"
                  inputmode="numeric"
                  maxlength="7"
                  autocomplete="off"
                  [value]="km()"
                  (input)="onNumero($event, km, 7)"
                  class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </label>
            </div>
            @if (id !== null) {
              <p class="-mt-2 text-xs text-slate-500">
                Altere o km aqui só para corrigir um erro de digitação.
              </p>
            }

            <div class="grid grid-cols-2 gap-3">
              <label class="block">
                <span class="text-sm font-medium text-slate-700">Venc. IPVA</span>
                <input
                  type="text"
                  inputmode="numeric"
                  maxlength="10"
                  autocomplete="off"
                  placeholder="dd/mm/aaaa"
                  [value]="ipva()"
                  (input)="onData($event, ipva)"
                  class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </label>
              <label class="block">
                <span class="text-sm font-medium text-slate-700">Venc. licenciamento</span>
                <input
                  type="text"
                  inputmode="numeric"
                  maxlength="10"
                  autocomplete="off"
                  placeholder="dd/mm/aaaa"
                  [value]="licenciamento()"
                  (input)="onData($event, licenciamento)"
                  class="mt-1 w-full h-12 rounded-lg border border-slate-300 px-3 text-base focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </label>
            </div>

            @if (erro(); as msg) {
              <p class="rounded-lg bg-red-50 text-red-700 text-sm p-3" role="alert">{{ msg }}</p>
            }

            <button
              type="button"
              (click)="salvar()"
              [disabled]="salvando()"
              class="w-full h-12 rounded-lg bg-blue-600 text-white font-semibold disabled:opacity-60"
            >
              {{ salvando() ? 'Salvando...' : 'Salvar' }}
            </button>

            @if (id !== null) {
              <button
                type="button"
                (click)="desativar()"
                [disabled]="salvando()"
                class="w-full h-12 rounded-lg border border-red-300 text-red-700 font-medium disabled:opacity-60"
              >
                Desativar veículo
              </button>
            }
          </div>
        }
      </section>
    </main>
  `,
})
export class VeiculoFormPage {
  private api = inject(FrotaApi);
  private router = inject(Router);
  private idDaRota = inject(ActivatedRoute).snapshot.paramMap.get('id');

  // null = cadastro de um veículo novo
  id = this.idDaRota ? Number(this.idDaRota) : null;
  titulo = this.id === null ? 'Novo veículo' : 'Editar veículo';

  tipos: { valor: TipoVeiculo; rotulo: string }[] = [
    { valor: 'CARRO', rotulo: 'Carro' },
    { valor: 'MOTO', rotulo: 'Moto' },
  ];

  placa = signal('');
  modelo = signal('');
  tipo = signal<TipoVeiculo | null>(null);
  ano = signal('');
  km = signal('');
  ipva = signal('');
  licenciamento = signal('');

  carregando = signal(this.id !== null);
  salvando = signal(false);
  erro = signal<string | null>(null);

  constructor() {
    if (this.id !== null) {
      this.api.veiculo(this.id).subscribe({
        next: (v) => {
          this.placa.set(v.placa);
          this.modelo.set(v.modelo);
          this.tipo.set(v.tipo);
          this.ano.set(String(v.ano));
          this.km.set(String(v.kmAtual));
          this.ipva.set(isoParaDataBr(v.vencimentoIpva));
          this.licenciamento.set(isoParaDataBr(v.vencimentoLicenciamento));
          this.carregando.set(false);
        },
        error: (e) => {
          this.erro.set(mensagemDeErro(e, 'Não foi possível carregar o veículo.'));
          this.carregando.set(false);
        },
      });
    }
  }

  // placa em maiúsculas e sem espaços ou traços enquanto digita
  onPlaca(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const limpa = campo.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    campo.value = limpa;
    this.placa.set(limpa);
  }

  // só números, com limite de dígitos
  onNumero(evento: Event, destino: WritableSignal<string>, maxDigitos: number): void {
    const campo = evento.target as HTMLInputElement;
    const limpo = campo.value.replace(/\D/g, '').slice(0, maxDigitos);
    campo.value = limpo;
    destino.set(limpo);
  }

  // data com máscara dd/mm/aaaa
  onData(evento: Event, destino: WritableSignal<string>): void {
    const campo = evento.target as HTMLInputElement;
    const mascarado = mascararData(campo.value);
    campo.value = mascarado;
    destino.set(mascarado);
  }

  salvar(): void {
    const placa = this.placa();
    const modelo = this.modelo().trim();
    const tipo = this.tipo();
    const ano = this.ano() ? Number(this.ano()) : null;
    const km = this.km() ? Number(this.km()) : null;
    const anoMaximo = new Date().getFullYear() + 1;

    if (!/^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$/.test(placa)) {
      this.erro.set('Placa inválida. Use o formato ABC1234 ou ABC1D23.');
      return;
    }
    if (!modelo) {
      this.erro.set('Informe o modelo.');
      return;
    }
    if (!tipo) {
      this.erro.set('Escolha se é carro ou moto.');
      return;
    }
    if (ano === null || this.ano().length !== 4 || ano < 1950 || ano > anoMaximo) {
      this.erro.set(`Informe um ano com 4 dígitos, entre 1950 e ${anoMaximo}.`);
      return;
    }
    if (km === null) {
      this.erro.set('Informe o km atual.');
      return;
    }

    const ipvaIso = this.ipva() ? dataBrParaIso(this.ipva()) : null;
    if (this.ipva() && !ipvaIso) {
      this.erro.set('Data do IPVA inválida. Use o formato dd/mm/aaaa.');
      return;
    }
    const licenciamentoIso = this.licenciamento() ? dataBrParaIso(this.licenciamento()) : null;
    if (this.licenciamento() && !licenciamentoIso) {
      this.erro.set('Data do licenciamento inválida. Use o formato dd/mm/aaaa.');
      return;
    }

    const req: VeiculoRequest = {
      placa,
      modelo,
      ano,
      tipo,
      kmAtual: km,
      vencimentoIpva: ipvaIso,
      vencimentoLicenciamento: licenciamentoIso,
    };

    this.erro.set(null);
    this.salvando.set(true);
    const chamada =
      this.id === null ? this.api.criarVeiculo(req) : this.api.atualizarVeiculo(this.id, req);
    chamada.subscribe({
      next: (v) => this.router.navigate(['/painel/veiculo', v.id]),
      error: (e) => {
        this.salvando.set(false);
        this.erro.set(mensagemDeErro(e, 'Não foi possível salvar o veículo.'));
      },
    });
  }

  desativar(): void {
    if (this.id === null) return;
    const ok = window.confirm(
      'Desativar este veículo? Ele sai do painel e do checklist, mas o histórico fica guardado.',
    );
    if (!ok) return;
    this.salvando.set(true);
    this.api.desativarVeiculo(this.id).subscribe({
      next: () => this.router.navigate(['/']),
      error: (e) => {
        this.salvando.set(false);
        this.erro.set(mensagemDeErro(e, 'Não foi possível desativar o veículo.'));
      },
    });
  }
}