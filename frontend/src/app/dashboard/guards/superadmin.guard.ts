import { CanMatchFn } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { inject } from '@angular/core';
import { first, map } from "rxjs/operators";

export const superadminGuard: CanMatchFn = (route, state) => {
  const authService = inject(AuthService);
  return authService.isAuthorized("SUPERADMIN").pipe(first(), map((result) => {
    if (result.err == null) {
      return result.ok;
    } else {
      return false;
    }
  }));
};
