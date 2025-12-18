import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentUpdateDialogComponent } from './payment-update-dialog.component';

describe('PaymentUpdateDialogComponent', () => {
  let component: PaymentUpdateDialogComponent;
  let fixture: ComponentFixture<PaymentUpdateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PaymentUpdateDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PaymentUpdateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
