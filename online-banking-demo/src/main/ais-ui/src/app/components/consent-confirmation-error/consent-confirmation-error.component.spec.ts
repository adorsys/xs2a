import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsentConfirmationErrorComponent } from './consent-confirmation-error.component';

describe('ConsentConfirmationErrorComponent', () => {
  let component: ConsentConfirmationErrorComponent;
  let fixture: ComponentFixture<ConsentConfirmationErrorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConsentConfirmationErrorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsentConfirmationErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
