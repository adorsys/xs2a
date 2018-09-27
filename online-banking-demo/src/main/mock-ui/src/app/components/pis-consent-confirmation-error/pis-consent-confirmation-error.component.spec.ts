import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PisConsentConfirmationErrorComponent } from './pis-consent-confirmation-error.component';

describe('PisConsentConfirmationErrorComponent', () => {
  let component: PisConsentConfirmationErrorComponent;
  let fixture: ComponentFixture<PisConsentConfirmationErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ PisConsentConfirmationErrorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PisConsentConfirmationErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
