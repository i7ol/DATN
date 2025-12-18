import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { InventoryResponse } from 'src/app/api/admin';

@Component({
  selector: 'app-inventory-edit-dialog',
  templateUrl: './inventory-edit-dialog.component.html',
  styleUrls: ['./inventory-edit-dialog.component.scss'],
})
export class InventoryEditDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<InventoryEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: InventoryResponse | null
  ) {
    this.form = this.fb.group({
      variantId: [data?.variantId ?? null, Validators.required],
      stock: [data?.stock ?? 0, Validators.required],
      sellingPrice: [data?.sellingPrice ?? 0, Validators.required],
      importPrice: [data?.importPrice ?? 0, Validators.required],
    });
  }

  save() {
    if (this.form.invalid) return;
    this.dialogRef.close(this.form.value);
  }

  close() {
    this.dialogRef.close(null);
  }
}
