/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
