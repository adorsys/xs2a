import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { PisTanConfirmationErrorComponent } from './pis-tan-confirmation-error.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('PisTanConfirmationErrorComponent', () => {
  let component: PisTanConfirmationErrorComponent;
  let fixture: ComponentFixture<PisTanConfirmationErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ RouterTestingModule ],
      declarations: [ PisTanConfirmationErrorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PisTanConfirmationErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
