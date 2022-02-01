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

package de.adorsys.psd2.xs2a.service.profile;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspspProfileServiceWrapperTest {
    private static final String ASPSP_SETTINGS_JSON_PATH = "json/service/profile/AspspSettings.json";
    public static final AspspSettings PROFILE = getProfile();

    private static final String INSTANCE_ID = "bank1";

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @BeforeEach
    void setUp() {
        when(requestProviderService.getInstanceId()).thenReturn(INSTANCE_ID);
    }

    @Test
    void getAvailableBookingStatuses() {
        // Given
        mockProfile();
        BookingStatus bookingStatus = BookingStatus.BOOKED;

        // When
        List<BookingStatus> actualAvailableStatuses = aspspProfileServiceWrapper.getAvailableBookingStatuses();

        // Then
        assertEquals(Collections.singletonList(bookingStatus), actualAvailableStatuses);
    }

    @Test
    void getSupportedTransactionStatusFormats() {
        // Given
        mockProfile();
        List<String> expectedFormats = Collections.singletonList("application/json");

        // When
        List<String> actualFormats = aspspProfileServiceWrapper.getSupportedTransactionStatusFormats();

        // Then
        assertEquals(expectedFormats, actualFormats);
    }

    @Test
    void getSupportedTransactionApplicationTypes() {
        // Given
        mockProfile();
        List<String> expected = Collections.singletonList("supportedTransactionApplicationTypes");

        // When
        List<String> actual = aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getSupportedPaymentTypeAndProductMatrix() {
        // Given
        mockProfile();
        Set<String> products = Collections.singleton("sepa-credit-transfers");
        Map<PaymentType, Set<String>> expected = Collections.singletonMap(PaymentType.SINGLE, products);

        // When
        Map<PaymentType, Set<String>> actual = aspspProfileServiceWrapper.getSupportedPaymentTypeAndProductMatrix();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getScaApproaches() {
        // Given
        List<ScaApproach> expected = Collections.singletonList(ScaApproach.DECOUPLED);
        when(aspspProfileService.getScaApproaches(INSTANCE_ID)).thenReturn(expected);

        // When
        List<ScaApproach> actual = aspspProfileServiceWrapper.getScaApproaches();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void isTppSignatureRequired() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isTppSignatureRequired();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAvailableAccountsConsentSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isAvailableAccountsConsentSupported();

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void isScaByOneTimeAvailableAccountsConsentRequired() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired();

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void isScaByOneTimeGlobalConsentRequired() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired();

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void isPsuInInitialRequestMandated() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isPsuInInitialRequestMandated();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isForceXs2aBaseLinksUrl() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isGlobalConsentSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isGlobalConsentSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isBankOfferedConsentSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isBankOfferedConsentSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isTransactionsWithoutBalancesSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isTransactionsWithoutBalancesSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isSigningBasketSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isSigningBasketSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isPaymentCancellationAuthorisationMandated() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isPaymentCancellationAuthorisationMandated();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isDebtorAccountOptionalInInitialRequest() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isDebtorAccountOptionalInInitialRequest();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isIbanValidationDisabled() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isIbanValidationDisabled();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isPsuInInitialRequestIgnored() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isPsuInInitialRequestIgnored();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isCheckUriComplianceToDomainSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isCheckUriComplianceToDomainSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAuthorisationConfirmationCheckByXs2a() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAuthorisationConfirmationRequestMandated() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isAuthorisationConfirmationRequestMandated();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isCheckTppRolesFromCertificateSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isCheckTppRolesFromCertificateSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isTrustedBeneficiariesSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isTrustedBeneficiariesSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAccountOwnerInformationSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isAccountOwnerInformationSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isEntryReferenceFromSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isEntryReferenceFromSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAisPisSessionsSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isAisPisSessionsSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isDeltaListSupported() {
        // Given
        mockProfile();

        // When
        boolean actual = aspspProfileServiceWrapper.isDeltaListSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void getPiisConsentSupported() {
        // Given
        mockProfile();

        // When
        PiisConsentSupported actual = aspspProfileServiceWrapper.getPiisConsentSupported();

        // Then
        assertThat(actual).isEqualTo(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
    }

    @Test
    void getPisRedirectUrlToAspsp() {
        // Given
        mockProfile();
        String expected = "http://localhost:4200/pis/{redirect-id}/{encrypted-payment-id}";

        // When
        String actual = aspspProfileServiceWrapper.getPisRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getSupportedPaymentCountryValidation() {
        // Given
        mockProfile();
        String expected = "DE";

        // When
        String actual = aspspProfileServiceWrapper.getSupportedPaymentCountryValidation();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getXs2aBaseLinksUrl() {
        // Given
        mockProfile();
        String expected = "http://myhost.com/";

        // When
        String actual = aspspProfileServiceWrapper.getXs2aBaseLinksUrl();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getOauthConfigurationUrl() {
        // Given
        mockProfile();
        String expected = "http://oauthConfigurationUrl.com/";

        // When
        String actual = aspspProfileServiceWrapper.getOauthConfigurationUrl();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getStartAuthorisationMode() {
        // Given
        mockProfile();
        StartAuthorisationMode expected = StartAuthorisationMode.AUTO;

        // When
        StartAuthorisationMode actual = aspspProfileServiceWrapper.getStartAuthorisationMode();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getScaRedirectFlow() {
        // Given
        mockProfile();
        ScaRedirectFlow expected = ScaRedirectFlow.REDIRECT;

        // When
        ScaRedirectFlow actual = aspspProfileServiceWrapper.getScaRedirectFlow();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getAisRedirectUrlToAspsp() {
        // Given
        mockProfile();
        String expected = "http://localhost:4200/ais/{redirect-id}/{encrypted-consent-id}";

        // When
        String actual = aspspProfileServiceWrapper.getAisRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getPiisRedirectUrlToAspsp() {
        // Given
        mockProfile();
        String expected = "http://localhost:4200/piis/{redirect-id}/{encrypted-consent-id}";

        // When
        String actual = aspspProfileServiceWrapper.getPiisRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getTppUriComplianceResponse() {
        // Given
        mockProfile();
        TppUriCompliance expected = TppUriCompliance.WARNING;

        // When
        TppUriCompliance actual = aspspProfileServiceWrapper.getTppUriComplianceResponse();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getMulticurrencyAccountLevel() {
        // Given
        mockProfile();
        MulticurrencyAccountLevel expected = MulticurrencyAccountLevel.SUBACCOUNT;

        // When
        MulticurrencyAccountLevel actual = aspspProfileServiceWrapper.getMulticurrencyAccountLevel();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getPisPaymentCancellationRedirectUrlToAspsp() {
        // Given
        mockProfile();
        String expected = "http://localhost:4200/pis/cancellation/{redirect-id}/{encrypted-payment-id}";

        // When
        String actual = aspspProfileServiceWrapper.getPisPaymentCancellationRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getSupportedAccountReferenceFields() {
        // Given
        mockProfile();
        List<SupportedAccountReferenceField> expected = Collections.singletonList(SupportedAccountReferenceField.IBAN);

        // When
        List<SupportedAccountReferenceField> actual = aspspProfileServiceWrapper.getSupportedAccountReferenceFields();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getNotificationSupportedModes() {
        // Given
        mockProfile();
        List<NotificationSupportedMode> expected = Collections.singletonList(NotificationSupportedMode.NONE);

        // When
        List<NotificationSupportedMode> actual = aspspProfileServiceWrapper.getNotificationSupportedModes();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getMaxConsentValidityDays() {
        // Given
        mockProfile();

        // When
        int actual = aspspProfileServiceWrapper.getMaxConsentValidityDays();

        // Then
        assertThat(actual).isEqualTo(0);
    }

    @Test
    void getAccountAccessFrequencyPerDay() {
        // Given
        mockProfile();

        // When
        int actual = aspspProfileServiceWrapper.getAccountAccessFrequencyPerDay();

        // Then
        assertThat(actual).isEqualTo(4);
    }

    @Test
    void getRedirectUrlExpirationTimeMs() {
        // Given
        mockProfile();

        // When
        long actual = aspspProfileServiceWrapper.getRedirectUrlExpirationTimeMs();

        // Then
        assertThat(actual).isEqualTo(600000);
    }

    @Test
    void getAuthorisationExpirationTimeMs() {
        // Given
        mockProfile();

        // When
        long actual = aspspProfileServiceWrapper.getAuthorisationExpirationTimeMs();

        // Then
        assertThat(actual).isEqualTo(86400000);
    }

    private void mockProfile() {
        when(aspspProfileService.getAspspSettings(INSTANCE_ID)).thenReturn(PROFILE);
    }

    private static AspspSettings getProfile() {
        return new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class);
    }
}
