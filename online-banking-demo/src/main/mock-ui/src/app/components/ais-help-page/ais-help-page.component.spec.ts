import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AisHelpPageComponent } from './ais-help-page.component';

describe('AisHelpPageComponent', () => {
  let component: AisHelpPageComponent;
  let fixture: ComponentFixture<AisHelpPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AisHelpPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AisHelpPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
