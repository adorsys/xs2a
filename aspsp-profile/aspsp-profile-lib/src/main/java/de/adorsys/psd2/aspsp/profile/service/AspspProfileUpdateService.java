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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.domain.BookingStatus;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;

import java.util.List;

//TODO refactor AspspProfileUpdateService and remove NOPMD comment https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/518
public interface AspspProfileUpdateService { //NOPMD class has update method for every option in profile as it should be

    void updateFrequencyPerDay(int frequencyPerDay);

    void updateCombinedServiceIndicator(boolean combinedServiceIndicator);

    void updateBankOfferedConsentSupport(boolean bankOfferedConsentSupport);

    void updateAvailablePaymentProducts(List<PaymentProduct> availablePaymentProducts);

    void updateAvailablePaymentTypes(List<PaymentType> availablePaymentTypes);

    void updateScaApproach(ScaApproach scaApproach);

    void updateTppSignatureRequired(boolean tppSignatureRequired);

    void updatePisRedirectUrlToAspsp(String redirectUrlToAspsp);

    void updateAisRedirectUrlToAspsp(String redirectUrlToAspsp);

    void updateMulticurrencyAccountLevel(MulticurrencyAccountLevel multicurrencyAccountLevel);

    void updateAvailableBookingStatuses(List<BookingStatus> availableBookingStatuses);

    void updateSupportedAccountReferenceFields(List<SupportedAccountReferenceField> fields);

    void updateConsentLifetime(int consentLifetime);

    void updateTransactionLifetime(int transactionLifetime);

    void updateAllPsd2Support(boolean allPsd2Support);

    void updateTransactionsWithoutBalancesSupported(boolean transactionsWithoutBalancesSupported);

    void updateSigningBasketSupported(boolean signingBasketSupported);

    void updatePaymentCancellationAuthorizationMandated(boolean paymentCancellationAuthorizationMandated);

    void updatePiisConsentSupported(boolean piisConsentSupported);

    void updateDeltaReportSupported(boolean deltaReportSupported);

    void updateRedirectUrlExpirationTimeMs(long redirectUrlExpirationTimeMs);

}
