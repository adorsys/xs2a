import { TestBed, inject, async } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BankingService } from './banking.service';

describe('BankingService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BankingService]
    });
  });

  it('should be created', async(inject([HttpTestingController, BankingService], (httpClient: HttpTestingController, service: BankingService) => {
    expect(service).toBeTruthy();
  })));
});
