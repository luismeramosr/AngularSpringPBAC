import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authenticationGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const isAuthenticated = authService.isAuthenticated();
  let router = new Router();

  if (isAuthenticated) {
    return true;
  } else {
    router.navigate(["/login"]);
    return false;
  }
};
