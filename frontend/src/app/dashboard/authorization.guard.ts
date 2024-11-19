import { CanMatchFn } from '@angular/router';

export const authorizationGuard: CanMatchFn = (route, segments) => {
  return true;
};
