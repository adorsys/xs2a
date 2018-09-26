import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AisTanConfirmationErrorComponent } from './ais-tan-confirmation-error.component';

describe('AisTanConfirmationErrorComponent', () => {
  let component: AisTanConfirmationErrorComponent;
  let fixture: ComponentFixture<AisTanConfirmationErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AisTanConfirmationErrorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisTanConfirmationErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
