import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppAuthGuard } from './app.authguard';

const routes: Routes = [
  { path: 'ais', loadChildren: './ais/ais.module#AisModule'},
  { path: 'pis', loadChildren: './pis/pis.module#PisModule'},
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [
    RouterModule
  ],
  providers: [
    AppAuthGuard
  ]
})
export class AppRoutingModule { }
