// import { Component, OnInit } from '@angular/core';
// import { ProductControllerService, ProductResponse } from 'src/app/api';
// import { CategoryControllerService } from 'src/app/api';

// @Component({
//   selector: 'product-list',
//   templateUrl: './product-list.component.html',
//   styleUrls: ['./product-list.component.scss'],
// })
// export class ProductListComponent implements OnInit {
//   products: ProductResponse[] = [];
//   categories: { id: number; name: string }[] = [];
//   loading = false;

//   showForm = false;
//   editingProduct: ProductResponse | null = null;
//   selectedFiles: File[] = [];
//   previewUrls: string[] = []; // preview tạm khi chọn file

//   form: any = {
//     name: '',
//     sku: '',
//     price: 0,
//     importPrice: 0,
//     categoryId: null,
//     imageUrl: '',
//   };

//   constructor(
//     private productApi: ProductControllerService,
//     private categoryApi: CategoryControllerService
//   ) {}

//   ngOnInit(): void {
//     this.loadProducts();
//     this.loadCategories();
//   }

//   // ================= LOAD PRODUCTS =================
//   loadProducts() {
//     this.loading = true;
//     this.productApi.getAllProducts('response').subscribe({
//       next: (resp: any) => {
//         const body = resp.body;
//         if (body instanceof Blob) {
//           body.text().then((text) => {
//             try {
//               const json = JSON.parse(text);
//               this.products = Array.isArray(json) ? json : json.data || [];
//             } catch (e) {
//               console.error('Parse products JSON failed', e);
//               this.products = [];
//             } finally {
//               this.loading = false;
//             }
//           });
//         } else {
//           this.products = Array.isArray(body) ? body : body.data || [];
//           this.loading = false;
//         }
//       },
//       error: (err) => {
//         console.error(err);
//         this.loading = false;
//       },
//     });
//   }

//   // ================= LOAD CATEGORIES =================
//   loadCategories() {
//     this.categoryApi.getAllCate('response').subscribe({
//       next: (resp: any) => {
//         const body = resp.body;
//         let arr: any[] = [];
//         if (body instanceof Blob) {
//           body.text().then((text) => {
//             try {
//               const json = JSON.parse(text);
//               arr = Array.isArray(json) ? json : json.data || [];
//               this.categories = arr
//                 .filter((c: any) => c.id != null && c.name != null)
//                 .map((c: any) => ({ id: c.id, name: c.name }));
//             } catch (e) {
//               console.error('Parse categories JSON failed', e);
//               this.categories = [];
//             }
//           });
//         } else {
//           arr = Array.isArray(body) ? body : body.data || [];
//           this.categories = arr
//             .filter((c: any) => c.id != null && c.name != null)
//             .map((c: any) => ({ id: c.id, name: c.name }));
//         }
//       },
//       error: (err) => console.error(err),
//     });
//   }

//   getCategoryName(categoryId: number | null): string {
//     if (!categoryId) return 'Chưa chọn';
//     const cat = this.categories.find((c) => c.id === categoryId);
//     return cat ? cat.name : 'Chưa chọn';
//   }

//   // ================= FORM HANDLERS =================
//   openAddForm() {
//     this.editingProduct = null;
//     this.showForm = true;
//     this.form = {
//       name: '',
//       sku: '',
//       price: 0,
//       importPrice: 0,
//       categoryId: null,
//       imageUrl: '',
//     };
//     this.selectedFiles = [];
//     this.previewUrls = [];
//   }

//   editProduct(product: ProductResponse) {
//     this.editingProduct = product;
//     this.showForm = true;
//     this.form = {
//       name: product.name,
//       sku: product.sku,
//       price: product.price,
//       importPrice: product.importPrice,
//       categoryId: product.categoryId || null,
//       imageUrl: product.images?.[0]?.url || '',
//     };
//     this.selectedFiles = [];
//     this.previewUrls = [];
//   }

//   cancelForm() {
//     this.showForm = false;
//     this.editingProduct = null;
//     this.selectedFiles = [];
//     this.previewUrls = [];
//   }

//   onFilesSelected(event: any) {
//     this.selectedFiles = Array.from(event.target.files);
//     this.previewUrls = this.selectedFiles.map((file) =>
//       URL.createObjectURL(file)
//     );
//   }

//   // ================= SAVE PRODUCT =================
//   saveProduct() {
//     // object JSON đúng kiểu generator yêu cầu
//     const productData = {
//       name: this.form.name,
//       sku: this.form.sku,
//       price: this.form.price,
//       importPrice: this.form.importPrice,
//       categoryId: this.form.categoryId ? Number(this.form.categoryId) : null,
//     };

//     // files là mảng Blob[]
//     const files: Blob[] = this.selectedFiles;

//     if (this.editingProduct) {
//       // update
//       this.productApi
//         .updateProduct(this.editingProduct.id!, productData, files, 'body')
//         .subscribe({
//           next: () => {
//             this.loadProducts();
//             this.cancelForm();
//           },
//           error: (err) => console.error(err),
//         });
//     } else {
//       // create
//       this.productApi.createProduct(productData, files, 'body').subscribe({
//         next: () => {
//           this.loadProducts();
//           this.cancelForm();
//         },
//         error: (err) => console.error(err),
//       });
//     }
//   }

//   // ================= DELETE PRODUCT =================
//   deleteProduct(id: number) {
//     if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
//     this.productApi.deleteProduct(id).subscribe({
//       next: () => this.loadProducts(),
//       error: (err) => console.error(err),
//     });
//   }
// }
