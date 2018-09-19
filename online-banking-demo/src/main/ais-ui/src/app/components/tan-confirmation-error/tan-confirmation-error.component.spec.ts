import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TanConfirmationErrorComponent } from './tan-confirmation-error.component';

describe('TanConfirmationErrorComponent', () => {
  let component: TanConfirmationErrorComponent;
  let fixture: ComponentFixture<TanConfirmationErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TanConfirmationErrorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TanConfirmationErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
