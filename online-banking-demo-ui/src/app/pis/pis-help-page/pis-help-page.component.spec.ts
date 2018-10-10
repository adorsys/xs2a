import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PisHelpPageComponent } from './pis-help-page.component';

describe('PisHelpPageComponent', () => {
  let component: PisHelpPageComponent;
  let fixture: ComponentFixture<PisHelpPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PisHelpPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PisHelpPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
