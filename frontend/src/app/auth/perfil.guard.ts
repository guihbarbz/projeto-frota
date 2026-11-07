import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, Perfil } from './auth.service';

export const perfilGuard =
  (...permitidos: Perfil[]): CanActivateFn =>
  () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.estaLogado()) return router.createUrlTree(['/login']);
    const perfil = auth.sessao()?.perfil;
    if (perfil && permitidos.includes(perfil)) return true;
    return router.createUrlTree([perfil === 'INSTRUTOR' ? '/checklist' : '/login']);
  };