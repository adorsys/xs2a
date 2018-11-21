import { PisModule } from './pis.module';

describe('PisModule', () => {
  let pisModule: PisModule;

  beforeEach(() => {
    pisModule = new PisModule();
  });

  it('should create an instance', () => {
    expect(pisModule).toBeTruthy();
  });
});
