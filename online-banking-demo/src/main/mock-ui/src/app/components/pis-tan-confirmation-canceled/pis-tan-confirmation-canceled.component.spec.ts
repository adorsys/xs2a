import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { PisTanConfirmationCanceledComponent } from './pis-tan-confirmation-canceled.component';

describe('PisTanConfirmationCanceledComponent', () => {
  let component: PisTanConfirmationCanceledComponent;
  let fixture: ComponentFixture<PisTanConfirmationCanceledComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ PisTanConfirmationCanceledComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PisTanConfirmationCanceledComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
