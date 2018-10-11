/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Address } from './address';
import { TheBICAssociatedToTheAccount_ } from './theBICAssociatedToTheAccount_';
import { AccountReference } from './accountReference';
import { Amount } from './amount';
import { PurposeCode } from './purposeCode';
import { Remittance } from './remittance';
import { TppInfo } from './tppInfo';
import { AccountReferenceNew } from './AccountReferenceNew';
import { AddressNew } from './AddressNew';

export interface PaymentsNew{

  paymentId?: number;

  /**
   * end to end authentication
   */
  endToEndIdentification?: string;
  /**
   * debtor account
   */
  debtorAccount?: AccountReferenceNew;
  /**
   * ultimate debtor
   */
  ultimateDebtor?: string;
  /**
   * currency --*
   */
  currency?: string;
  /**
   * amount --*
   */
  amount?: number;
  /**
   * debtor account
   */
  creditorAccount?: AccountReferenceNew;
  /**
   * creditor agent
   */
  creditorAgent?: string;
  /**
   * creditor name
   */
  creditorName?: string;
  /**
  * creditor Address
  */
  creditorAddress?: AddressNew;
  /**
   * remittance information unstructured
   */
  remittanceInformationUnstructured?: string;
  /**
   * remittance information structured
   */
  remittanceInformationStructured?: Remittance;
  /**
   * instructed amount
   */
  instructedAmount?: Amount;
  /**
   * requested execution date
   */
  requestedExecutionDate?: string;
  /**
   * requested execution time
   */
  requestedExecutionTime?: string;
  /**
   * ultimate creditor
   */
  ultimateCreditor?: string;
  /**
   * purpose code
   */
  purposeCode?: string;
  /**
   * dayOfExecution --*
   */
  dayOfExecution?: number;


  endDate?:string;
  executionId?: number;
  executionRule?: string;
  frequency?: string;
  startDate?: string;
  tppInfo?: TppInfo;
}
