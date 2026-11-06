import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { LoginPage } from './pages/login/login.page';
import { InicioPage } from './pages/inicio/inicio.page';
import { ChecklistListaPage } from './pages/checklist-lista/checklist-lista.page';
import { ChecklistVeiculoPage } from './pages/checklist-veiculo/checklist-veiculo.page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: '', component: InicioPage, canActivate: [authGuard] },
  { path: 'checklist', component: ChecklistListaPage, canActivate: [authGuard] },
  { path: 'checklist/:id', component: ChecklistVeiculoPage, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];