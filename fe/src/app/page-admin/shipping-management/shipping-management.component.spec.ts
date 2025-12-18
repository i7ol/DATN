import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShippingManagementComponent } from './shipping-management.component';

describe('ShippingManagementComponent', () => {
  let component: ShippingManagementComponent;
  let fixture: ComponentFixture<ShippingManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShippingManagementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShippingManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
