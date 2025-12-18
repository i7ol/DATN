import { Component, Inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ProductResponse } from 'src/app/api/admin';
import { ProductAdminControllerService } from 'src/app/api/admin';
import { CategoryAdminControllerService } from 'src/app/api/admin';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

interface ProductForm {
  name: string;
  sku: string;
  price: number;
  importPrice: number;
  categoryId: number;
  description?: string;
}

interface ProductVariant {
  id?: number;
  sizeName: string;
  color: string;
}

interface ProductImage {
  id?: number;
  url: string;
  file?: File;
}

interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'warning' | 'error' | 'success' | 'info' | 'question';
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
  images: ProductImage[] = [];
  deletedImageIds: number[] = [];

  form: ProductForm = {
    name: '',
    sku: '',
    price: 0,
    importPrice: 0,
    categoryId: 0,
    description: '',
  };
  variants: ProductVariant[] = [];
  showForm = false;
  addingCategory = false;
  categoryForm: { name: string } = { name: '' };

  // Loading states
  isLoading = false;
  isSaving = false;
  isDeleting = false;

  constructor(
    @Inject(ProductAdminControllerService)
    private productApi: ProductAdminControllerService,
    @Inject(CategoryAdminControllerService)
    private categoryApi: CategoryAdminControllerService,
    private http: HttpClient,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }
  getEndItem(): number {
    if (!this.totalItems) return 0;
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalItems);
  }

  get totalPagesArray(): number[] {
    const totalPages = Math.ceil(this.totalItems / this.pageSize);
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  // ---------------- LOAD PRODUCTS ----------------
  loadProducts() {
    this.isLoading = true;
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
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error loading products:', err);
          this.toastr.error('Không thể tải danh sách sản phẩm', 'Lỗi!');
          this.isLoading = false;
        },
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
      error: (err) => {
        console.error('Error loading categories:', err);
        this.toastr.error('Không thể tải danh mục', 'Lỗi!');
      },
    });
  }

  getCategoryName(id: number | null) {
    return this.categories.find((c) => c.id === id)?.name || 'Chưa chọn';
  }

  // ---------------- IMAGE HANDLING ----------------
  checkImageUrl(url?: string): string {
    if (!url) return 'assets/img/duck.webp';

    if (url.startsWith('data:image')) {
      return url;
    }

    if (url.startsWith('/uploads')) {
      return `http://localhost:8081${url}`;
    }

    return url;
  }

  handleImageError(event: any) {
    const img = event.target;
    if (!img.dataset.fallback) {
      img.dataset.fallback = 'true';
      img.src = 'assets/img/duck.webp';
    }
  }

  // ---------------- FORM ----------------
  openAddForm() {
    this.editingProduct = null;
    this.showForm = true;
    this.images = [];
    this.deletedImageIds = [];
    this.variants = [];
    this.form = {
      name: '',
      sku: '',
      price: 0,
      importPrice: 0,
      categoryId: 0,
      description: '',
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
      description: p.description || '',
    };

    this.variants =
      p.variants?.map((v) => ({
        id: v.id,
        sizeName: v.sizeName,
        color: v.color,
      })) || [];

    this.images =
      p.images?.map((img) => ({
        id: img.id,
        url: this.checkImageUrl(img.url),
      })) || [];
    this.deletedImageIds = [];
  }

  onFileSelected(event: any) {
    if (!event.target.files) return;
    const filesArray = Array.from(event.target.files);

    // Validate file types and size
    const maxSize = 5 * 1024 * 1024; // 5MB
    const validTypes = ['image/jpeg', 'image/png', 'image/webp'];

    filesArray.forEach((file: any) => {
      if (!validTypes.includes(file.type)) {
        this.toastr.warning(
          'Chỉ chấp nhận file ảnh (JPEG, PNG, WebP)',
          'Cảnh báo!'
        );
        return;
      }

      if (file.size > maxSize) {
        this.toastr.warning(
          'Kích thước file không được vượt quá 5MB',
          'Cảnh báo!'
        );
        return;
      }

      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.images.push({
          url: e.target.result as string,
          file: file,
        });
      };
      reader.readAsDataURL(file);
    });
  }

  addVariant() {
    this.variants.push({
      id: undefined,
      sizeName: '',
      color: '',
    });
  }

  removeVariant(index: number) {
    this.variants.splice(index, 1);
  }

  removeImage(index: number) {
    const img = this.images[index];
    if (img.id) {
      this.deletedImageIds.push(img.id);
    }
    this.images.splice(index, 1);
  }

  // ---------------- FORM DATA BUILDING ----------------
  buildFormData(): FormData {
    const formData = new FormData();

    const productData: any = {
      name: this.form.name?.trim(),
      sku: this.form.sku?.trim(),
      price: this.form.price,
      importPrice: this.form.importPrice,
      categoryId: Number(this.form.categoryId),
      description: this.form.description || '',
      deletedImageIds: this.deletedImageIds ?? [],
    };

    if (this.deletedImageIds && this.deletedImageIds.length > 0) {
      productData.deletedImageIds = [...this.deletedImageIds];
    }

    formData.append(
      'product',
      new Blob([JSON.stringify(productData)], { type: 'application/json' })
    );

    const newImages = this.images.filter((img) => !img.id && img.file);
    newImages.forEach((img, index) => {
      if (img.file) {
        formData.append('files', img.file, img.file.name);
      }
    });

    if (this.variants && this.variants.length > 0) {
      const variantData = this.variants
        .map((v) => {
          const variant: any = {
            sizeName: v.sizeName?.trim() || '',
            color: v.color?.trim() || '',
          };
          if (v.id) variant.id = v.id;
          return variant;
        })
        .filter((v) => v.sizeName || v.color);

      if (variantData.length > 0) {
        formData.append(
          'variants',
          new Blob([JSON.stringify(variantData)], { type: 'application/json' })
        );
      }
    }

    return formData;
  }

  // ---------------- SAVE PRODUCT ----------------
  saveProduct() {
    // Validation
    const errors: string[] = [];

    if (!this.form.name.trim()) errors.push('Tên sản phẩm không được để trống');
    if (!this.form.sku.trim()) errors.push('SKU không được để trống');
    if (this.form.price <= 0) errors.push('Giá bán phải lớn hơn 0');
    if (this.form.importPrice <= 0) errors.push('Giá nhập phải lớn hơn 0');
    if (!this.form.categoryId) errors.push('Vui lòng chọn danh mục');

    if (errors.length > 0) {
      errors.forEach((error) => this.toastr.warning(error, 'Cảnh báo!'));
      return;
    }

    this.isSaving = true;
    const formData = this.buildFormData();

    if (this.editingProduct) {
      this.updateProductWithFormData(formData);
    } else {
      this.createProductWithFormData(formData);
    }
  }

  createProductWithFormData(formData: FormData) {
    this.http
      .post('http://localhost:8081/api/admin/products', formData, {
        observe: 'response',
      })
      .subscribe({
        next: (response: any) => {
          this.toastr.success('Thêm sản phẩm thành công!', 'Thành công!');
          this.showForm = false;
          this.loadProducts();
          this.isSaving = false;
        },
        error: (err) => {
          console.error('CREATE ERROR:', err);
          this.toastr.error('Không thể thêm sản phẩm', 'Lỗi!');
          this.isSaving = false;
        },
      });
  }

  updateProductWithFormData(formData: FormData) {
    if (!this.editingProduct) return;

    this.http
      .put(
        `http://localhost:8081/api/admin/products/${this.editingProduct.id}`,
        formData,
        {
          observe: 'response',
        }
      )
      .subscribe({
        next: (response: any) => {
          this.toastr.success('Cập nhật sản phẩm thành công!', 'Thành công!');
          this.showForm = false;
          this.loadProducts();
          this.isSaving = false;
        },
        error: (err) => {
          console.error('UPDATE ERROR:', err);
          this.toastr.error('Không thể cập nhật sản phẩm', 'Lỗi!');
          this.isSaving = false;
        },
      });
  }

  // ---------------- DELETE PRODUCT ----------------
  async deleteProduct(p: ProductResponse) {
    const result = await Swal.fire({
      title: 'Xác nhận xóa',
      text: `Bạn có chắc chắn muốn xóa sản phẩm "${p.name}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      reverseButtons: true,
      customClass: {
        confirmButton: 'px-4 py-2',
        cancelButton: 'px-4 py-2',
      },
    });

    if (result.isConfirmed) {
      this.isDeleting = true;
      this.productApi.deleteProduct(p.id!, 'response').subscribe({
        next: () => {
          this.toastr.success('Xóa sản phẩm thành công!', 'Thành công!');
          this.loadProducts();
          this.isDeleting = false;
        },
        error: (err) => {
          console.error('Delete error:', err);
          this.toastr.error('Không thể xóa sản phẩm', 'Lỗi!');
          this.isDeleting = false;
        },
      });
    }
  }

  // ---------------- CATEGORY MANAGEMENT ----------------
  openAddCategoryForm() {
    this.addingCategory = true;
    this.categoryForm = { name: '' };
  }

  cancelAddCategory() {
    this.addingCategory = false;
    this.categoryForm = { name: '' };
  }

  createCategory() {
    if (!this.categoryForm.name.trim()) {
      this.toastr.warning('Tên danh mục không được để trống', 'Cảnh báo!');
      return;
    }

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
          this.toastr.success('Thêm danh mục thành công!', 'Thành công!');
        },
        error: (err) => {
          console.error('Create category error:', err);
          this.toastr.error('Không thể thêm danh mục', 'Lỗi!');
        },
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
    if (!c.tempName?.trim()) {
      this.toastr.warning('Tên danh mục không được để trống', 'Cảnh báo!');
      return;
    }

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
          this.toastr.success('Cập nhật danh mục thành công!', 'Thành công!');
        },
        error: (err) => {
          console.error('Update category error:', err);
          this.toastr.error('Không thể cập nhật danh mục', 'Lỗi!');
        },
      });
  }

  async deleteCategory(c: any) {
    const result = await Swal.fire({
      title: 'Xác nhận xóa',
      text: `Bạn có chắc chắn muốn xóa danh mục "${c.name}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      reverseButtons: true,
      customClass: {
        confirmButton: 'px-4 py-2',
        cancelButton: 'px-4 py-2',
      },
    });

    if (result.isConfirmed) {
      this.categoryApi.deleteCate(c.id, 'response').subscribe({
        next: () => {
          this.categories = this.categories.filter((cat) => cat.id !== c.id);
          this.toastr.success('Xóa danh mục thành công!', 'Thành công!');
        },
        error: (err) => {
          console.error('Delete category error:', err);
          this.toastr.error('Không thể xóa danh mục', 'Lỗi!');
        },
      });
    }
  }

  // ---------------- UTILITY METHODS ----------------
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(amount);
  }

  closeForm() {
    this.showForm = false;
    this.editingProduct = null;
  }
}
