import { Component, OnInit } from '@angular/core';
import { InventoryAdminControllerService } from 'src/app/api/admin/api/inventoryAdminController.service';

import {
  InventoryResponse,
  InventoryRequest,
  ImportRequest,
  ExportRequest,
  AdjustRequest,
} from 'src/app/api/admin';

@Component({
  selector: 'app-inventory-list',
  templateUrl: './inventory-list.component.html',
  styleUrls: ['./inventory-list.component.scss'],
})
export class InventoryListComponent implements OnInit {
  inventories: InventoryResponse[] = [];
  loading = false;

  page = 0;
  size = 10;
  total = 0;

  showForm = false;
  editing = false;
  form = this.emptyForm();

  showDelete = false;
  deleteData: InventoryResponse | null = null;

  constructor(private api: InventoryAdminControllerService) {}

  ngOnInit(): void {
    this.loadData();
  }

  emptyForm() {
    return {
      id: null,
      variantId: null,
      stock: 0,
      importPrice: 0,
      sellingPrice: 0,
    };
  }

  get totalPagesArray(): number[] {
    const pages = Math.ceil(this.total / this.size) || 1;
    return Array.from({ length: pages }, (_, i) => i);
  }

  async loadData() {
    this.loading = true;
    try {
      const resp = await this.api
        .getAll(this.page, this.size, 'body')
        .toPromise();
      this.inventories = resp.content || [];
      this.total = resp.totalElements || 0;
    } finally {
      this.loading = false;
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadData();
    }
  }
  nextPage() {
    if ((this.page + 1) * this.size < this.total) {
      this.page++;
      this.loadData();
    }
  }
  goTo(i: number) {
    this.page = i;
    this.loadData();
  }

  openAddForm() {
    this.editing = false;
    this.form = this.emptyForm();
    this.showForm = true;
  }

  openEditForm(inv: InventoryResponse) {
    this.editing = true;
    this.form = {
      id: inv.id ?? null,
      variantId: inv.variantId ?? null,
      stock: inv.stock ?? 0,
      importPrice: inv.importPrice ?? 0,
      sellingPrice: inv.sellingPrice ?? 0,
    };
    this.showForm = true;
  }

  saveInventory() {
    if (!this.form.variantId) return alert('Thiếu Variant ID');

    const req: InventoryRequest = {
      id: this.form.id ?? undefined,
      variantId: this.form.variantId!,
      stock: this.form.stock ?? 0,
      importPrice: this.form.importPrice ?? 0,
      sellingPrice: this.form.sellingPrice ?? 0,
    };

    this.api.createOrUpdate(req).subscribe({
      next: () => {
        this.showForm = false;
        this.loadData();
      },
      error: () => alert('Lưu thất bại'),
    });
  }

  quickImport(inv: InventoryResponse) {
    const payload: ImportRequest = {
      variantId: inv.variantId!,
      quantity: 10,
      importPrice: inv.importPrice ?? 0,
      note: 'Quick import +10',
    };
    this.api.importStock(payload).subscribe(() => this.loadData());
  }

  quickExport(inv: InventoryResponse) {
    const payload: ExportRequest = {
      variantId: inv.variantId!,
      quantity: 1,
      note: 'Quick export -1',
    };
    this.api.exportStock(payload).subscribe(() => this.loadData());
  }

  confirmDelete(inv: InventoryResponse) {
    this.deleteData = inv;
    this.showDelete = true;
  }

  deleteInventory() {
    if (!this.deleteData) return;
    const adjust: AdjustRequest = {
      newStock: 0,
      reason: 'Delete inventory -> set stock to 0',
    };
    this.api.adjust(this.deleteData.variantId!, adjust).subscribe(() => {
      this.showDelete = false;
      this.deleteData = null;
      this.loadData();
    });
  }
}
