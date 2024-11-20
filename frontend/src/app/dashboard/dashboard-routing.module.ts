import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { userGuard } from './guards/user.guard';
import { adminGuard } from './guards/admin.guard';
import { superadminGuard } from './guards/superadmin.guard';

const routes: Routes = [
  { path: "home", loadComponent: () => import("./pages/home/home.component").then(c => c.HomeComponent) },
  { path: "users", loadComponent: () => import("./pages/users/user/user.component").then(c => c.UserComponent), canMatch: [userGuard] },
  { path: "users", loadComponent: () => import("./pages/users/admin/admin.component").then(c => c.AdminComponent), canMatch: [adminGuard] },
  { path: "users", loadComponent: () => import("./pages/users/super-admin/super-admin.component").then(c => c.SuperAdminComponent), canMatch: [superadminGuard] },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }
