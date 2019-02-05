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

package de.adorsys.psd2.aspsp.profile.config;

import de.adorsys.psd2.aspsp.profile.domain.BookingStatus;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class BankProfileSetting {
    /**
     * This field indicates the requested maximum frequency for an access per day
     */
    private int frequencyPerDay;

    /**
     * If "true" indicates that a payment initiation service will be addressed in the same "session"
     */
    private boolean combinedServiceIndicator;

    /**
     * List of SCA Approach supported by ASPSP ordered by priority
     */
    private List<ScaApproach> scaApproaches;

    /**
     * A signature of the request by the TPP on application level.
     * If the value is `true`, the signature is mandated by ASPSP.
     * If the value is `false`, the signature can be omitted.
     */
    private boolean tppSignatureRequired;

    /**
     * URL to ASPSP service in order to to work with PIS
     */
    private String pisRedirectUrlToAspsp;

    /**
     * URL to ASPSP service in order to work with AIS
     */
    private String aisRedirectUrlToAspsp;

    /**
     * Multicurrency account types supported by ASPSP
     */
    private MulticurrencyAccountLevel multicurrencyAccountLevel;

    /**
     * If "true" indicates that an ASPSP supports "Bank Offered Consent" consent model
     */
    private boolean bankOfferedConsentSupport;

    /**
     * Booking statuses supported by ASPSP, such as Booked, Pending and Both
     */
    private List<BookingStatus> availableBookingStatuses = new ArrayList<>();

    /**
     * Account Reference fields supported by ASPSP, such as: IBAN, PAN, MSIDN
     */
    private List<SupportedAccountReferenceField> supportedAccountReferenceFields = new ArrayList<>();

    /**
     * The limit of a maximum lifetime of consent set in days
     */
    private int consentLifetime;

    /**
     * The limit of a maximum lifetime of transaction set in days
     */
    private int transactionLifetime;

    /**
     * AllPsd2Support status, that shows if ASPSP supports Global consents
     */
    private boolean allPsd2Support;

    /**
     * If "false" indicates that an ASPSP might add balance information to transactions list
     */
    private boolean transactionsWithoutBalancesSupported;

    /**
     * If the option is set to "false", than ASPSP does not support signing basket
     */
    private boolean signingBasketSupported;

    /**
     * If the option is set to "true", then authorization of the payment cancellation is mandated by the ASPSP
     */
    private boolean paymentCancellationAuthorizationMandated;

    /**
     * If the option is set to "true", than PIIS consent should be stored in CMS
     */
    private boolean piisConsentSupported;

    /**
     * If the option is set to "true", than Delta report is supported
     */
    private boolean deltaReportSupported;

    /**
     * The limit of an expiration time of redirect url set in milliseconds
     */
    private long redirectUrlExpirationTimeMs;

    /**
     * URL to ASPSP service in order to to work with PIS payment cancellation
     */
    private String pisPaymentCancellationRedirectUrlToAspsp;

    /**
     * The limit of an expiration time of not confirmed consent url set in milliseconds
     */
    private long notConfirmedConsentExpirationPeriodMs;

    /**
     * The limit of an expiration time of not confirmed payment url set in milliseconds
     */
    private long notConfirmedPaymentExpirationPeriodMs;

    /**
     * A matrix payment-product/payment-type which allows to choose needed types
     */
    private Map<PaymentType, Set<String>> supportedPaymentTypeAndProductMatrix;

    /**
     * This field contains the limit of an expiration time of redirect url for payment cancellation set in milliseconds
     */
    private long paymentCancellationRedirectUrlExpirationTimeMs;

    /**
     * If "true" indicates that an ASPSP supports available accounts for consent
     */
    private boolean availableAccountsConsentSupported;

    /**
     * If "true" indicates that an ASPSP requires usage of SCA to validate a one-time available accounts consent
     */
    private boolean scaByOneTimeAvailableAccountsConsentRequired;
}
