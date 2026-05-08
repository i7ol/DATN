import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReturnAdminComponent } from './return-admin.component';

describe('ReturnAdminComponent', () => {
  let component: ReturnAdminComponent;
  let fixture: ComponentFixture<ReturnAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReturnAdminComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReturnAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
