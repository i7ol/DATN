import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';

// User Components
import { ProductListComponent } from './page-user/product-user/product-list/product-list.component';
import { ProductDetailComponent } from './page-user/product-user/product-detail/product-detail.component';
import { CartComponent } from './page-user/cart/cart.component';
import { CheckoutComponent } from './page-user/checkout/checkout.component';
import { ProfileComponent } from './page-user/profile/profile.component';
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
import { MyOrderComponent } from './page-user/my-order/my-order.component';
import { MyOrderDetailComponent } from './page-user/my-order-detail/my-order-detail.component';
import { UserListComponent } from './page-admin/user-management/user-list/user-list.component';
import { RoleManagementComponent } from './page-admin/user-management/role-management/role-management.component';
import { ReturnUserComponent } from './page-user/return/return-user/return-user.component';
import { ReturnAdminComponent } from './page-admin/return/return-admin/return-admin.component';
import { ReturnCreateComponent } from './page-user/return/return-user/return-user-create.component';
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
    path: 'my-orders',
    component: MyOrderComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'my-orders/:id',
    component: MyOrderDetailComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'checkout',
    component: CheckoutComponent,
  },
  {
    path: 'returns-user',
    component: ReturnUserComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'returns',
    component: ReturnUserComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'returns/create',
    component: ReturnCreateComponent,
    canActivate: [AuthGuard],
  },

  // Admin protected routes
  {
    path: 'admin',
    canActivate: [AuthGuard],
    canActivateChild: [AdminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: UserListComponent },
      { path: 'roles', component: RoleManagementComponent },
      {
        path: 'returns-admin',
        component: ReturnAdminComponent,
      },
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
