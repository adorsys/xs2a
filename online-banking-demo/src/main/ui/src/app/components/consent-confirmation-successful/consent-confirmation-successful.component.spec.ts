import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ConsentConfirmationSuccessfulComponent } from './consent-confirmation-successful.component';

describe('ConsentConfirmationSuccessfulComponent', () => {
  let component: ConsentConfirmationSuccessfulComponent;
  let fixture: ComponentFixture<ConsentConfirmationSuccessfulComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ ConsentConfirmationSuccessfulComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsentConfirmationSuccessfulComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
