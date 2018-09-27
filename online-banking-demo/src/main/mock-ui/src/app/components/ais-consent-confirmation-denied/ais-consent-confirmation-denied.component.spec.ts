import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AisConsentConfirmationDeniedComponent } from './ais-consent-confirmation-denied.component';

describe('AisConsentConfirmationDeniedComponent', () => {
  let component: AisConsentConfirmationDeniedComponent;
  let fixture: ComponentFixture<AisConsentConfirmationDeniedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AisConsentConfirmationDeniedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisConsentConfirmationDeniedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
