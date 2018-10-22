import { AisModule } from './ais.module';

describe('AisModule', () => {
  let aisModule: AisModule;

  beforeEach(() => {
    aisModule = new AisModule();
  });

  it('should create an instance', () => {
    expect(aisModule).toBeTruthy();
  });
});
