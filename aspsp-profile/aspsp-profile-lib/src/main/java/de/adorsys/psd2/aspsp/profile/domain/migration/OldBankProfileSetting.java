/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.domain.migration;

import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
public class OldBankProfileSetting {
    private int frequencyPerDay;
    private boolean combinedServiceIndicator;
    private List<ScaApproach> scaApproaches = new ArrayList<>();
    private boolean tppSignatureRequired;
    private String pisRedirectUrlToAspsp;
    private String aisRedirectUrlToAspsp;
    private MulticurrencyAccountLevel multicurrencyAccountLevel;
    private boolean bankOfferedConsentSupport;
    private List<BookingStatus> availableBookingStatuses = new ArrayList<>();
    private List<SupportedAccountReferenceField> supportedAccountReferenceFields = new ArrayList<>();
    private int consentLifetime;
    private int transactionLifetime;
    private boolean allPsd2Support;
    private boolean transactionsWithoutBalancesSupported;
    private boolean signingBasketSupported;
    private boolean paymentCancellationAuthorizationMandated;
    private boolean piisConsentSupported;
    private boolean deltaListSupported;
    private long redirectUrlExpirationTimeMs;
    private long authorisationExpirationTimeMs;
    private String pisPaymentCancellationRedirectUrlToAspsp;
    private long notConfirmedConsentExpirationPeriodMs;
    private long notConfirmedPaymentExpirationPeriodMs;
    private Map<String, Set<String>> supportedPaymentTypeAndProductMatrix;
    private long paymentCancellationRedirectUrlExpirationTimeMs;
    private boolean availableAccountsConsentSupported;
    private boolean scaByOneTimeAvailableAccountsConsentRequired;
    private boolean psuInInitialRequestMandated;
    private boolean forceXs2aBaseUrl;
    private String xs2aBaseUrl;
    private ScaRedirectFlow scaRedirectFlow;
    private boolean entryReferenceFromSupported;
    private List<String> supportedTransactionApplicationTypes;
    private String startAuthorisationMode;
}
