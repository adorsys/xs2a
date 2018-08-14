import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsentConfirmationPageComponent } from './consent-confirmation-page.component';

describe('ConsentConfirmationPageComponent', () => {
  let component: ConsentConfirmationPageComponent;
  let fixture: ComponentFixture<ConsentConfirmationPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConsentConfirmationPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsentConfirmationPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
