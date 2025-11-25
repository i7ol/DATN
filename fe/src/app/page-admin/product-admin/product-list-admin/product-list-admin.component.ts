import { Component, OnInit } from '@angular/core';
import { ProductControllerService, ProductResponse } from 'src/app/api';
import { CategoryControllerService } from 'src/app/api';

interface ProductForm {
  name: string;
  sku: string;
  price: number;
  importPrice: number;
  categoryId: number;
  imageUrl: string;
}

interface CategoryForm {
  name: string;
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

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;

  // PRODUCT FORM
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

  // CATEGORY INLINE ADD
  addingCategory = false;
  categoryForm: CategoryForm = { name: '' };

  // CATEGORY TABLE
  showCategoryTable = false;

  // DELETE CONFIRM
  showDeleteConfirm = false;
  productToDeleteId: number | null = null;
  productToDeleteData: ProductResponse | null = null;

  constructor(
    private productApi: ProductControllerService,
    private categoryApi: CategoryControllerService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  // ------------------ PRODUCTS ------------------
  loadProducts() {
    this.productApi
      .getAllProducts(this.currentPage, this.pageSize, 'response')
      .subscribe({
        next: (resp: any) => {
          const body =
            resp.body instanceof Blob
              ? resp.body
                  .text()
                  .then((text) => this.mapProductResponse(JSON.parse(text)))
              : this.mapProductResponse(resp.body);
        },
      });
  }

  private mapProductResponse(res: any) {
    this.products = res.content || [];
    this.totalItems = res.totalElements || 0;
    this.pageSize = res.size || this.pageSize;
    this.currentPage = res.number || 0;
  }

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize);
  }
  get totalPagesArray(): number[] {
    return Array(this.totalPages);
  }

  prevPage() {
    if (this.currentPage > 0) this.currentPage--;
    this.loadProducts();
  }
  nextPage() {
    if (this.currentPage < this.totalPages - 1) this.currentPage++;
    this.loadProducts();
  }
  goToPage(page: number) {
    this.currentPage = page;
    this.loadProducts();
  }

  // ------------------ CATEGORIES ------------------
  loadCategories() {
    this.categoryApi.getAllCate('response').subscribe({
      next: (resp: any) => {
        const body =
          resp.body instanceof Blob
            ? resp.body
                .text()
                .then((text) => (this.categories = JSON.parse(text)))
            : (this.categories = resp.body);
      },
    });
  }

  getCategoryName(id: number | null) {
    const cat = this.categories.find((c) => c.id === id);
    return cat ? cat.name : 'Chưa chọn';
  }

  // INLINE ADD
  openAddCategoryForm() {
    this.addingCategory = true;
    this.categoryForm = { name: '' };
  }

  cancelAddCategory() {
    this.addingCategory = false;
    this.categoryForm = { name: '' };
  }

  createCategory() {
    this.categoryApi.createCate(this.categoryForm).subscribe({
      next: () => {
        this.addingCategory = false;
        this.loadCategories();
      },
      error: () => {
        alert('Thêm danh mục thất bại!');
      },
    });
  }

  // INLINE EDIT
  startEditCategory(c: any) {
    c.editing = true;
    c.tempName = c.name;
  }

  saveEditCategory(c: any) {
    this.categoryApi.updateCate(c.id, { name: c.tempName }).subscribe({
      next: () => {
        c.name = c.tempName;
        c.editing = false;
      },
      error: () => {
        alert('Cập nhật thất bại!');
      },
    });
  }

  cancelEditCategory(c: any) {
    c.editing = false;
  }

  // DELETE
  deleteCategory(categoryId: number) {
    if (!confirm('Bạn có chắc muốn xóa danh mục này?')) return;
    this.categoryApi.deleteCate(categoryId).subscribe({
      next: () => this.loadCategories(),
      error: () => alert('Xóa danh mục thất bại!'),
    });
  }

  // ------------------ PRODUCT FORM ------------------
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
  saveProduct() {
    if (this.editingProduct) this.updateProduct();
    else this.createProduct();
  }
  createProduct() {
    const productJson = JSON.stringify({ ...this.form });
    this.productApi.createProduct(productJson, this.selectedFiles).subscribe({
      next: () => {
        this.showForm = false;
        this.selectedFiles = [];
        this.previewUrls = [];
        this.loadProducts();
      },
    });
  }
  updateProduct() {
    if (!this.editingProduct) return;
    const productJson = JSON.stringify({ ...this.form });
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

  // ------------------ DELETE CONFIRM ------------------
  openDeleteConfirm(p: ProductResponse) {
    this.productToDeleteId = p.id!;
    this.productToDeleteData = p;
    this.showDeleteConfirm = true;
  }
  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.productToDeleteId = null;
    this.productToDeleteData = null;
  }
  confirmDelete() {
    if (!this.productToDeleteId) return;
    this.productApi.deleteProduct(this.productToDeleteId).subscribe({
      next: () => {
        this.closeDeleteConfirm();
        this.loadProducts();
      },
    });
  }
}
