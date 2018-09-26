import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AisTanConfirmationSuccessfulComponent } from './ais-tan-confirmation-successful.component';

describe('AisTanConfirmationSuccessfulComponent', () => {
  let component: AisTanConfirmationSuccessfulComponent;
  let fixture: ComponentFixture<AisTanConfirmationSuccessfulComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AisTanConfirmationSuccessfulComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisTanConfirmationSuccessfulComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
