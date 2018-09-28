import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AisTanConfirmationCanceledComponent } from './ais-tan-confirmation-canceled.component';

describe('AisTanConfirmationCanceledComponent', () => {
  let component: AisTanConfirmationCanceledComponent;
  let fixture: ComponentFixture<AisTanConfirmationCanceledComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AisTanConfirmationCanceledComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisTanConfirmationCanceledComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
