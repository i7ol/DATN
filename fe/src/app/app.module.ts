import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
// import { ProductListComponent } from './page-user/product-user/product-list/product-list.component';
import { HttpClientModule } from '@angular/common/http';
import { ApiAdminModule } from './api/admin';
import { ApiUserModule } from './api/user';
import { MatPaginatorModule } from '@angular/material/paginator';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { ProductListComponent } from './page-user/product-user/product-list/product-list.component';
import { ProductListAdminComponent } from './page-admin/product-admin/product-list-admin/product-list-admin.component';
import { ProductDetailComponent } from './page-user/product-user/product-detail/product-detail.component';
import { InventoryListComponent } from './page-admin/inventory-list/inventory-list.component';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

// Angular Material
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon'; // nếu có icon
import { MatSortModule } from '@angular/material/sort'; // nếu muốn sort
import { MatSelectModule } from '@angular/material/select'; // nếu có select

import { InventoryEditDialogComponent } from './page-admin/inventory-list/inventory-edit-dialog.component';
import { HeaderComponent } from './shared/header/header.component';
@NgModule({
  declarations: [
    AppComponent,
    ProductListAdminComponent,
    ProductListComponent,
    ProductDetailComponent,
    InventoryListComponent,
    InventoryEditDialogComponent,
    HeaderComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    MatTableModule,
    MatIconModule,
    MatSortModule,
    MatSelectModule,
  ],

  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
