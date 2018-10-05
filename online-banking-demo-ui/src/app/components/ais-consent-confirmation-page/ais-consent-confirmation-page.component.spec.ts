import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { AisConsentConfirmationPageComponent } from './ais-consent-confirmation-page.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientModule } from '@angular/common/http';

describe('AisConsentConfirmationPageComponent', () => {
  let component: AisConsentConfirmationPageComponent;
  let fixture: ComponentFixture<AisConsentConfirmationPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientModule],
      declarations: [ AisConsentConfirmationPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisConsentConfirmationPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
