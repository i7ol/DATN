import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';

// User Components
import { ProductListComponent } from './page-user/product-user/product-list/product-list.component';
import { ProductDetailComponent } from './page-user/product-user/product-detail/product-detail.component';
import { CartComponent } from './page-user/cart/cart.component';
import { CheckoutComponent } from './page-user/checkout/checkout.component';

// Admin Components
import { ProductListAdminComponent } from './page-admin/product-admin/product-list-admin/product-list-admin.component';
import { InventoryListComponent } from './page-admin/inventory-list/inventory-list.component';
import { OrderListComponent } from './page-admin/order-admin/order-list/order-list.component';
import { OrderDetailComponent } from './page-admin/order-admin/order-detail/order-detail.component';
import { PaymentListComponent } from './page-admin/payment-list/payment-list.component';
import { ShippingListComponent } from './page-admin/shipping-list/shipping-list.component';
import { ShippingManagementComponent } from './page-admin/shipping-management/shipping-management.component';
import { AdminDashboardComponent } from './page-admin/admin-dashboard/admin-dashboard.component';
import { PaymentResultComponent } from './page-user/payment-result/payment-result.component';
const routes: Routes = [
  // Public routes
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  { path: 'products', component: ProductListComponent },
  { path: 'product/:id', component: ProductDetailComponent },
  { path: 'cart', component: CartComponent },
  { path: 'payment/result', component: PaymentResultComponent },
  {
    path: 'payment-result',
    component: PaymentResultComponent,
  },

  // User protected routes
  {
    path: 'checkout',
    component: CheckoutComponent,
  },

  // Admin protected routes
  {
    path: 'admin',
    canActivate: [AuthGuard, AdminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },

      // Order Management
      { path: 'orders', component: OrderListComponent },
      { path: 'orders/:id', component: OrderDetailComponent },
      { path: 'orders/:id/shipping', component: ShippingManagementComponent },

      // Payment Management
      { path: 'payments', component: PaymentListComponent },

      // Shipping Management
      { path: 'shippings', component: ShippingListComponent },
      { path: 'shippings/:id', component: ShippingManagementComponent },

      // Product Management
      { path: 'products-admin', component: ProductListAdminComponent },
      { path: 'inventory', component: InventoryListComponent },
    ],
  },

  // Fallback route
  { path: '**', redirectTo: 'products' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
