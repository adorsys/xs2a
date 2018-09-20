import { Balance } from './balance';

export class Account {
  id: string;
  iban: string;
  bban: string;
  currency: string;
  name: string;
  accountType: string;
  balances: [Balance];
  _links: {
    viewBalances: string;
    viewTransactions: string;
  };
}

