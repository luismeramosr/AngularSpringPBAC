import { inject } from '@angular/core';
import { CanMatchFn } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { first, map } from "rxjs/operators";

export const userGuard: CanMatchFn = (route, state) => {
  const authService = inject(AuthService);
  return authService.isAuthorized("USUARIO").pipe(first(), map((result) => {
    if (result.err == null) {
      return result.ok;
    } else {
      return false;
    }
  }));
};
