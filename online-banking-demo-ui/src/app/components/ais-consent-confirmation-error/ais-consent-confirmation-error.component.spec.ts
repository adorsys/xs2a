import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AisConsentConfirmationErrorComponent } from './ais-consent-confirmation-error.component';

describe('AisConsentConfirmationErrorComponent', () => {
  let component: AisConsentConfirmationErrorComponent;
  let fixture: ComponentFixture<AisConsentConfirmationErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AisConsentConfirmationErrorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisConsentConfirmationErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
