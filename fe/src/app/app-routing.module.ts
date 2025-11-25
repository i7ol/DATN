import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProductListAdminComponent } from './page-admin/product-admin/product-list-admin/product-list-admin.component';
const routes: Routes = [
  // {
  //   path: 'products',
  //   component: ProductListComponent,
  // },
  {
    path: 'products-admin',
    component: ProductListAdminComponent,
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
