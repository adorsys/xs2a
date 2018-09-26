import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { PisTanConfirmationPageComponent } from './pis-tan-confirmation-page.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';


describe('PisTanConfirmationPageComponent', () => {
  let component: PisTanConfirmationPageComponent;
  let fixture: ComponentFixture<PisTanConfirmationPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, RouterTestingModule, HttpClientTestingModule ],
      declarations: [ PisTanConfirmationPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PisTanConfirmationPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

