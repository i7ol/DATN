import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastrModule } from 'ngx-toastr';
import { ApiUserModule, Configuration } from 'src/app/api/user';
// Material Modules
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatStepperModule } from '@angular/material/stepper';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTooltipModule } from '@angular/material/tooltip';

// Components-admin
import { HeaderComponent } from './shared/header/header.component';
import { AdminDashboardComponent } from './page-admin/admin-dashboard/admin-dashboard.component';
import { OrderListComponent } from './page-admin/order-admin/order-list/order-list.component';
import { OrderDetailComponent } from './page-admin/order-admin/order-detail/order-detail.component';
import { OrderAdminControllerService } from './api/admin/api/orderAdminController.service';
import { ShippingListComponent } from './page-admin/shipping-list/shipping-list.component';
import { ShippingManagementComponent } from './page-admin/shipping-management/shipping-management.component';
import { SimpleDialogComponent } from './shared/components/simple-dialog/simple-dialog.component';
import { ShippingAdminControllerService } from './api/admin/api/shippingAdminController.service';
import { StatusUpdateDialogComponent } from './shared/components/status-update-dialog/status-update-dialog.component';
import { PaymentUpdateDialogComponent } from './shared/components/payment-update-dialog/payment-update-dialog.component';
import { PaymentListComponent } from './page-admin/payment-list/payment-list.component';
import { ProductListAdminComponent } from './page-admin/product-admin/product-list-admin/product-list-admin.component';
import { InventoryListComponent } from './page-admin/inventory-list/inventory-list.component';

// Components-user
import { CartComponent } from './page-user/cart/cart.component';
import { CheckoutComponent } from './page-user/checkout/checkout.component';
import { ProductListComponent } from './page-user/product-user/product-list/product-list.component';
import { ProductDetailComponent } from './page-user/product-user/product-detail/product-detail.component';
import { CartService } from './page-user/cart/cart.service';
// Guards & Interceptors
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { AdminGuard } from './core/guards/admin.guard';
import { AuthGuard } from './core/guards/auth.guard';
import { AuthModalComponent } from './shared/components/auth-modal/auth-modal.component';
import { PaymentResultComponent } from './page-user/payment-result/payment-result.component';
import { ConfirmDialogComponent } from './shared/confirm-dialog/confirm-dialog.component';
@NgModule({
  declarations: [
    AppComponent,
    AuthModalComponent,
    HeaderComponent,
    AdminDashboardComponent,
    OrderListComponent,
    OrderDetailComponent,
    PaymentListComponent,
    ShippingListComponent,
    ShippingManagementComponent,
    StatusUpdateDialogComponent,
    PaymentUpdateDialogComponent,
    CartComponent,
    CheckoutComponent,
    SimpleDialogComponent,
    ProductListAdminComponent,
    InventoryListComponent,
    ProductListComponent,
    ProductDetailComponent,
    PaymentResultComponent,
    ConfirmDialogComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    CommonModule,
    // Material Modules
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTabsModule,
    MatCardModule,
    MatToolbarModule,
    MatMenuModule,
    MatBadgeModule,
    MatStepperModule,
    MatCheckboxModule,
    MatRadioModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTooltipModule,
    ToastrModule.forRoot({
      positionClass: 'toast-top-right',
      timeOut: 3000,
      closeButton: true,
      progressBar: true,
      preventDuplicates: true,
    }),
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    AuthGuard,
    CartService,
    {
      provide: 'CartService',
      useExisting: CartService,
    },
    AdminGuard,
    OrderAdminControllerService,
    ShippingAdminControllerService,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
