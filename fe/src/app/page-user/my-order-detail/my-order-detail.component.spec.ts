import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyOrderDetailComponent } from './my-order-detail.component';

describe('MyOrderDetailComponent', () => {
  let component: MyOrderDetailComponent;
  let fixture: ComponentFixture<MyOrderDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyOrderDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyOrderDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
