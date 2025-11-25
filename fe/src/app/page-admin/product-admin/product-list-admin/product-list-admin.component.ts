import { Component, OnInit } from '@angular/core';
import { ProductControllerService, ProductResponse } from 'src/app/api';
import { CategoryControllerService } from 'src/app/api';
import { PageEvent } from '@angular/material/paginator';

interface ProductForm {
  name: string;
  sku: string;
  price: number;
  importPrice: number;
  categoryId: number;
  imageUrl: string;
}

@Component({
  selector: 'product-list-admin',
  templateUrl: './product-list-admin.component.html',
  styleUrls: ['./product-list-admin.component.scss'],
})
export class ProductListAdminComponent implements OnInit {
  products: ProductResponse[] = [];
  categories: { id: number; name: string }[] = [];

  // Pagination
  loading = false;
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;

  // Form
  showForm = false;
  editingProduct: ProductResponse | null = null;
  selectedFiles: File[] = [];
  previewUrls: string[] = [];

  form: ProductForm = {
    name: '',
    sku: '',
    price: 0,
    importPrice: 0,
    categoryId: 0,
    imageUrl: '',
  };

  constructor(
    private productApi: ProductControllerService,
    private categoryApi: CategoryControllerService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
  }

  // -------------------------------------------------------
  // LOAD PRODUCTS (Pagination)
  // -------------------------------------------------------
  loadProducts() {
    this.loading = true;

    this.productApi
      .getAllProducts(this.currentPage, this.pageSize, 'response')
      .subscribe({
        next: (resp: any) => {
          const body = resp.body;
          if (body instanceof Blob) {
            body.text().then((text) => {
              const json = JSON.parse(text);
              this.mapProductResponse(json);
              this.loading = false;
            });
          } else {
            this.mapProductResponse(body);
            this.loading = false;
          }
        },
        error: () => (this.loading = false),
      });
  }

  private mapProductResponse(res: any) {
    this.products = res.content || [];
    this.totalItems = res.totalElements || 0;
    this.pageSize = res.size || this.pageSize;
    this.currentPage = res.number || 0;
  }

  onPageChange(event: PageEvent) {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.loadProducts();
  }

  // -------------------------------------------------------
  // LOAD CATEGORIES
  // -------------------------------------------------------
  loadCategories() {
    this.categoryApi.getAllCate('response').subscribe({
      next: (resp: any) => {
        const body = resp.body;
        if (body instanceof Blob) {
          body.text().then((text) => {
            const json = JSON.parse(text);
            this.categories = json;
          });
        } else {
          this.categories = body;
        }
      },
    });
  }

  getCategoryName(id: number | null): string {
    const cat = this.categories.find((c) => c.id === id);
    return cat ? cat.name : 'Chưa chọn';
  }

  // -------------------------------------------------------
  // FORM MODAL
  // -------------------------------------------------------
  openAddForm() {
    this.editingProduct = null;
    this.showForm = true;
    this.selectedFiles = [];
    this.previewUrls = [];

    this.form = {
      name: '',
      sku: '',
      price: 0,
      importPrice: 0,
      categoryId: 0,
      imageUrl: '',
    };
  }

  editProduct(p: ProductResponse) {
    this.editingProduct = p;
    this.showForm = true;

    this.form = {
      name: p.name,
      sku: p.sku,
      price: p.price,
      importPrice: p.importPrice,
      categoryId: p.categoryId || 0,
      imageUrl: p.images?.[0]?.url || '',
    };

    this.previewUrls = p.images?.map((img) => img.url) || [];
  }

  onFileSelected(event: any) {
    if (!event.target.files) return;

    this.selectedFiles = Array.from(event.target.files);
    this.previewUrls = [];

    this.selectedFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onload = (e) => this.previewUrls.push(e.target?.result as string);
      reader.readAsDataURL(file);
    });
  }

  // -------------------------------------------------------
  // CRUD
  // -------------------------------------------------------
  saveProduct() {
    if (this.editingProduct) {
      this.updateProduct();
    } else {
      this.createProduct();
    }
  }

  // CREATE -------------------------------------------------
  createProduct() {
    const productJson = JSON.stringify({
      name: this.form.name,
      sku: this.form.sku,
      price: this.form.price,
      importPrice: this.form.importPrice,
      categoryId: this.form.categoryId,
      imageUrl: this.form.imageUrl,
    });

    this.productApi.createProduct(productJson, this.selectedFiles).subscribe({
      next: () => {
        this.showForm = false;
        this.selectedFiles = [];
        this.previewUrls = [];
        this.loadProducts();
      },
    });
  }

  // UPDATE -------------------------------------------------
  updateProduct() {
    if (!this.editingProduct) return;

    const productJson = JSON.stringify({
      name: this.form.name,
      sku: this.form.sku,
      price: this.form.price,
      importPrice: this.form.importPrice,
      categoryId: this.form.categoryId,
      imageUrl: this.form.imageUrl,
    });

    this.productApi
      .updateProduct(this.editingProduct.id!, productJson, this.selectedFiles)
      .subscribe({
        next: () => {
          this.showForm = false;
          this.selectedFiles = [];
          this.previewUrls = [];
          this.loadProducts();
        },
      });
  }

  // DELETE -------------------------------------------------
  deleteProduct(id: number) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;

    this.productApi.deleteProduct(id).subscribe({
      next: () => this.loadProducts(),
    });
  }
}
