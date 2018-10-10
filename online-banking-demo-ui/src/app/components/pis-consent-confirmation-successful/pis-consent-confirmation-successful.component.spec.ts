import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PisConsentConfirmationSuccessfulComponent } from './pis-consent-confirmation-successful.component';

describe('PisConsentConfirmationSuccessfulComponent', () => {
  let component: PisConsentConfirmationSuccessfulComponent;
  let fixture: ComponentFixture<PisConsentConfirmationSuccessfulComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ PisConsentConfirmationSuccessfulComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PisConsentConfirmationSuccessfulComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
