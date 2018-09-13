import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { TanConfirmationPageComponent } from './tan-confirmation-page.component';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';


describe('TanConfirmationPageComponent', () => {
  let component: TanConfirmationPageComponent;
  let fixture: ComponentFixture<TanConfirmationPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, RouterTestingModule, HttpClientTestingModule ],
      declarations: [ TanConfirmationPageComponent]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TanConfirmationPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
