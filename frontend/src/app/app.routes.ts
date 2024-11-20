import { Routes } from '@angular/router';
import { authenticationGuard } from './core/auth/authentication.guard';

export const routes: Routes = [
  { path: "login", loadComponent: () => import("./login/login.component").then(c => c.LoginComponent) },
  { path: "dashboard", loadChildren: () => import("./dashboard/dashboard.module").then(m => m.DashboardModule), canActivate: [authenticationGuard] },
  { path: "", redirectTo: "/login", pathMatch: "full" },
  { path: "**", redirectTo: "/login" }
];
