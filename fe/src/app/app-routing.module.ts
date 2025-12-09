import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProductListAdminComponent } from './page-admin/product-admin/product-list-admin/product-list-admin.component';
import { ProductListComponent } from './page-user/product-user/product-list/product-list.component';
import { ProductDetailComponent } from './page-user/product-user/product-detail/product-detail.component';
import { InventoryListComponent } from './page-admin/inventory-list/inventory-list.component';
import { CartComponent } from './page-user/cart/cart.component';

const routes: Routes = [
  {
    path: 'products',
    component: ProductListComponent,
  },
  {
    path: 'product/:id',
    component: ProductDetailComponent,
  },

  {
    path: 'products-admin',
    component: ProductListAdminComponent,
  },
  {
    path: 'inventory',
    component: InventoryListComponent,
  },
  {
    path: 'cart',
    component: CartComponent,
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
