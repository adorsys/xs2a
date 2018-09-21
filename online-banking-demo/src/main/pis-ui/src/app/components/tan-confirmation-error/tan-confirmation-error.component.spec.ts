import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { TanConfirmationErrorComponent } from './tan-confirmation-error.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('TanConfirmationErrorComponent', () => {
  let component: TanConfirmationErrorComponent;
  let fixture: ComponentFixture<TanConfirmationErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ RouterTestingModule ],
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
