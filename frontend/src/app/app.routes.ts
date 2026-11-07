import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { perfilGuard } from './auth/perfil.guard';
import { LoginPage } from './pages/login/login.page';
import { PainelPage } from './pages/painel/painel.page';
import { VeiculoDetalhePage } from './pages/veiculo-detalhe/veiculo-detalhe.page';
import { VeiculoFormPage } from './pages/veiculo-form/veiculo-form.page';
import { ChecklistListaPage } from './pages/checklist-lista/checklist-lista.page';
import { ChecklistVeiculoPage } from './pages/checklist-veiculo/checklist-veiculo.page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: '', component: PainelPage, canActivate: [perfilGuard('GESTOR')] },
  { path: 'painel/veiculo/:id', component: VeiculoDetalhePage, canActivate: [perfilGuard('GESTOR')] },
  { path: 'painel/veiculos/novo', component: VeiculoFormPage, canActivate: [perfilGuard('GESTOR')] },
  { path: 'painel/veiculos/:id/editar', component: VeiculoFormPage, canActivate: [perfilGuard('GESTOR')] },
  { path: 'checklist', component: ChecklistListaPage, canActivate: [authGuard] },
  { path: 'checklist/:id', component: ChecklistVeiculoPage, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];