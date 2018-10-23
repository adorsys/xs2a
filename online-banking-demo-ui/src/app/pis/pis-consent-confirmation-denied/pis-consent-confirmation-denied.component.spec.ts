import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PisConsentConfirmationDeniedComponent } from './pis-consent-confirmation-denied.component';

describe('PisConsentConfirmationDeniedComponent', () => {
  let component: PisConsentConfirmationDeniedComponent;
  let fixture: ComponentFixture<PisConsentConfirmationDeniedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ PisConsentConfirmationDeniedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PisConsentConfirmationDeniedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
