import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { AisTanConfirmationPageComponent } from './ais-tan-confirmation-page.component';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';


describe('AisTanConfirmationPageComponent', () => {
  let component: AisTanConfirmationPageComponent;
  let fixture: ComponentFixture<AisTanConfirmationPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, RouterTestingModule, HttpClientTestingModule ],
      declarations: [ AisTanConfirmationPageComponent]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisTanConfirmationPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
