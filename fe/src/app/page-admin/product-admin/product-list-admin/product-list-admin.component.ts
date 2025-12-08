import { Component, Inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  ProductResponse,
  ProductCreateRequest,
  ProductUpdateRequest,
} from 'src/app/api/admin';

import { ProductAdminControllerService } from 'src/app/api/admin';
import { CategoryAdminControllerService } from 'src/app/api/admin';

interface ProductForm {
  name: string;
  sku: string;
  price: number;
  importPrice: number;
  categoryId: number;
}
interface ProductVariant {
  sizeName: string;
  color: string;
}
@Component({
  selector: 'product-list-admin',
  templateUrl: './product-list-admin.component.html',
  styleUrls: ['./product-list-admin.component.scss'],
})
export class ProductListAdminComponent implements OnInit {
  products: ProductResponse[] = [];
  categories: {
    id: number;
    name: string;
    editing?: boolean;
    tempName?: string;
  }[] = [];
  showCategoryTable = false;
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;

  editingProduct: ProductResponse | null = null;
  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  deletedImageIds: number[] = [];

  form: ProductForm = {
    name: '',
    sku: '',
    price: 0,
    importPrice: 0,
    categoryId: 0,
  };
  variants: ProductVariant[] = [];
  showDeleteConfirm = false;
  productToDeleteId: number | null = null;
  showForm = false;
  addingCategory = false;
  categoryForm: { name: string } = { name: '' };

  constructor(
    @Inject(ProductAdminControllerService)
    private productApi: ProductAdminControllerService,
    @Inject(CategoryAdminControllerService)
    private categoryApi: CategoryAdminControllerService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  get totalPagesArray(): number[] {
    return Array.from(
      { length: Math.ceil(this.totalItems / this.pageSize) },
      (_, i) => i
    );
  }

  // ---------------- LOAD PRODUCTS ----------------
  loadProducts() {
    this.productApi
      .getAllProducts(this.currentPage, this.pageSize, 'response')
      .subscribe({
        next: async (resp: any) => {
          let body;
          if (resp.body instanceof Blob) {
            const text = await resp.body.text();
            body = JSON.parse(text);
          } else {
            body = resp.body;
          }
          this.products = body.content;
          this.totalItems = body.totalElements;
        },
        error: (err) => console.error(err),
      });
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadProducts();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPagesArray.length - 1) {
      this.currentPage++;
      this.loadProducts();
    }
  }

  goToPage(page: number) {
    this.currentPage = page;
    this.loadProducts();
  }

  // ---------------- CATEGORIES ----------------
  loadCategories() {
    this.categoryApi.getAllCate('response').subscribe({
      next: async (resp: any) => {
        let body;
        if (resp.body instanceof Blob) {
          const text = await resp.body.text();
          body = JSON.parse(text);
        } else {
          body = resp.body;
        }
        this.categories = body;
      },
      error: (err) => console.error(err),
    });
  }

  getCategoryName(id: number | null) {
    return this.categories.find((c) => c.id === id)?.name || 'Chưa chọn';
  }

  // ---------------- FORM ----------------
  openAddForm() {
    this.editingProduct = null;
    this.showForm = true;
    this.selectedFiles = [];
    this.previewUrls = [];
    this.variants = [];
    this.form = { name: '', sku: '', price: 0, importPrice: 0, categoryId: 0 };
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
    };
    this.variants =
      p.variants?.map((v) => ({ sizeName: v.sizeName, color: v.color })) || [];
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

  buildFormData(): FormData {
    const formData = new FormData();

    const productData: any = {
      name: this.form.name,
      sku: this.form.sku,
      price: this.form.price,
      importPrice: this.form.importPrice,
      categoryId: this.form.categoryId,
    };

    // Thêm danh sách ảnh cần xóa
    if (this.deletedImageIds.length > 0) {
      productData.deletedImageIds = this.deletedImageIds;
    }

    formData.append(
      'product',
      new Blob([JSON.stringify(productData)], { type: 'application/json' })
    );

    // Thêm file mới
    this.selectedFiles.forEach((file) => formData.append('files', file));

    // Thêm variants
    if (this.variants.length) {
      formData.append(
        'variants',
        new Blob([JSON.stringify(this.variants)], { type: 'application/json' })
      );
    }

    return formData;
  }

  async saveProduct() {
    if (this.editingProduct) await this.updateProduct();
    else await this.createProduct();
  }

  createProduct() {
    const formData = this.buildFormData();
    this.http
      .post('http://localhost:8081/api/admin/products', formData)
      .subscribe({
        next: () => {
          this.showForm = false;
          this.selectedFiles = [];
          this.previewUrls = [];
          this.loadProducts();
        },
        error: (err) => console.error('Create product error:', err),
      });
  }

  updateProduct() {
    if (!this.editingProduct) return;
    const formData = this.buildFormData();
    this.http
      .put(
        `http://localhost:8081/api/admin/products/${this.editingProduct.id}`,
        formData
      )
      .subscribe({
        next: () => {
          this.showForm = false;
          this.selectedFiles = [];
          this.previewUrls = [];
          this.loadProducts();
        },
        error: (err) => console.error('Update product error:', err),
      });
  }

  addVariant() {
    this.variants.push({ sizeName: '', color: '' });
  }

  removeVariant(index: number) {
    this.variants.splice(index, 1);
  }

  removeImage(index: number) {
    const removedUrl = this.previewUrls[index];

    // Xóa khỏi mảng preview
    this.previewUrls.splice(index, 1);

    // Xóa ảnh mới upload
    if (this.selectedFiles[index]) {
      this.selectedFiles.splice(index, 1);
    }
    // Nếu là ảnh cũ (đã có id)
    else if (this.editingProduct && this.editingProduct.images) {
      const imgObj = this.editingProduct.images.find(
        (img) => img.url === removedUrl
      );
      if (imgObj && imgObj.id) {
        this.deletedImageIds.push(imgObj.id);
      }
    }
  }

  // ---------------- DELETE ----------------
  openDeleteConfirm(p: ProductResponse) {
    this.productToDeleteId = p.id!;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.productToDeleteId = null;
  }

  confirmDelete() {
    if (!this.productToDeleteId) return;
    this.productApi
      .deleteProduct(this.productToDeleteId, 'response')
      .subscribe({
        next: () => {
          this.closeDeleteConfirm();
          this.loadProducts();
        },
        error: (err) => console.error(err),
      });
  }

  // ---------------- CATEGORY ----------------
  openAddCategoryForm() {
    this.addingCategory = true;
    this.categoryForm = { name: '' };
  }

  cancelAddCategory() {
    this.addingCategory = false;
    this.categoryForm = { name: '' };
  }

  createCategory() {
    if (!this.categoryForm.name.trim()) return;
    this.categoryApi
      .createCate({ name: this.categoryForm.name.trim() }, 'response')
      .subscribe({
        next: async (resp: any) => {
          let body;
          if (resp.body instanceof Blob) {
            const text = await resp.body.text();
            body = JSON.parse(text);
          } else {
            body = resp.body;
          }
          this.categories.push(body);
          this.cancelAddCategory();
        },
        error: (err) => console.error(err),
      });
  }

  startEditCategory(c: any) {
    c.editing = true;
    c.tempName = c.name;
  }

  cancelEditCategory(c: any) {
    c.editing = false;
    c.tempName = '';
  }

  saveEditCategory(c: any) {
    if (!c.tempName?.trim()) return;
    this.categoryApi
      .updateCate(c.id, { name: c.tempName.trim() }, 'response')
      .subscribe({
        next: async (resp: any) => {
          let body;
          if (resp.body instanceof Blob) {
            const text = await resp.body.text();
            body = JSON.parse(text);
          } else {
            body = resp.body;
          }
          c.name = body.name;
          c.editing = false;
        },
        error: (err) => console.error(err),
      });
  }

  deleteCategory(id: number) {
    if (!id) return;
    this.categoryApi.deleteCate(id, 'response').subscribe({
      next: () => {
        this.categories = this.categories.filter((c) => c.id !== id);
      },
      error: (err) => console.error(err),
    });
  }
}
