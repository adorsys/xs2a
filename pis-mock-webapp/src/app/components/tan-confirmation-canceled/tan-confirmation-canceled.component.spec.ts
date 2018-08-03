import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TanConfirmationCanceledComponent } from './tan-confirmation-canceled.component';

describe('TanConfirmationCanceledComponent', () => {
  let component: TanConfirmationCanceledComponent;
  let fixture: ComponentFixture<TanConfirmationCanceledComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ TanConfirmationCanceledComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TanConfirmationCanceledComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
