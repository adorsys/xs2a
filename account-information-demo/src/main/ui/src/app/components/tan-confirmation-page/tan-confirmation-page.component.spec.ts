import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TanConfirmationPageComponent } from './tan-confirmation-page.component';

describe('TanConfirmationPageComponent', () => {
  let component: TanConfirmationPageComponent;
  let fixture: ComponentFixture<TanConfirmationPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TanConfirmationPageComponent ]
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
