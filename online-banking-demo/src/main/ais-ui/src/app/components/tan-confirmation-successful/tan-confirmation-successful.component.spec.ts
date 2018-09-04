import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TanConfirmationSuccessfulComponent } from './tan-confirmation-successful.component';

describe('TanConfirmationSuccessfulComponent', () => {
  let component: TanConfirmationSuccessfulComponent;
  let fixture: ComponentFixture<TanConfirmationSuccessfulComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TanConfirmationSuccessfulComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TanConfirmationSuccessfulComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
