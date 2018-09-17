import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsentConfirmationDeniedComponent } from './consent-confirmation-denied.component';

describe('ConsentConfirmationDeniedComponent', () => {
  let component: ConsentConfirmationDeniedComponent;
  let fixture: ComponentFixture<ConsentConfirmationDeniedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConsentConfirmationDeniedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsentConfirmationDeniedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
