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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RedirectLinkBuilderTest {

    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String ENC_PMNT_ID = "encryptedPaymentId";
    private static final String ENC_CNSNT_ID = "encryptedConsentId";
    private static final String REDIRECT_ID = "redirectId";

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @InjectMocks
    private RedirectLinkBuilder redirectLinkBuilder;

    @Test
    void buildConsentScaRedirectLink() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getAisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID, "", ConsentType.AIS);

        assertEquals("something/Authorisation123/Consent123/{encrypted-payment-id}/Consent123/Authorisation123/{encrypted-payment-id}/something-else", redirectLink);
    }

    @Test
    void buildPiisConsentScaRedirectLink() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getPiisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID, "", ConsentType.PIIS_TPP);

        assertEquals("something/Authorisation123/Consent123/{encrypted-payment-id}/Consent123/Authorisation123/{encrypted-payment-id}/something-else", redirectLink);
    }

    @Test
    void buildConsentScaRedirectLinkWithInstanceId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else?instanceId={instance-id}").when(aspspProfileService).getAisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID, "bank1", ConsentType.AIS);

        assertEquals("something/Authorisation123/Consent123/{encrypted-payment-id}/Consent123/Authorisation123/{encrypted-payment-id}/something-else?instanceId=bank1", redirectLink);
    }

    @Test
    void buildConsentScaRedirectLinkWithoutInstanceId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getAisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID, "bank1", ConsentType.AIS);

        assertEquals("something/Authorisation123/Consent123/{encrypted-payment-id}/Consent123/Authorisation123/{encrypted-payment-id}/something-else", redirectLink);
    }

    @Test
    void buildConsentScaRedirectLink_exception() {

        assertThrows(UnsupportedOperationException.class,
                     () -> redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID, "bank1", ConsentType.PIIS_ASPSP));
    }

    @Test
    void buildPaymentScaRedirectLink() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getPisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "");

        assertEquals("something/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else", redirectLink);
    }

    @Test
    void buildPaymentScaRedirectLinkWithInstanceId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getPisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "bank1");

        assertEquals("something/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else", redirectLink);
    }

    @Test
    void buildPaymentCancellationScaRedirectLink() {
        doReturn("cancellation/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getPisPaymentCancellationRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "");

        assertEquals("cancellation/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else", redirectLink);
    }

    @Test
    void buildPaymentCancellationScaRedirectLinkWithInstanceId() {
        doReturn("cancellation/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else").when(aspspProfileService).getPisPaymentCancellationRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "bank1");

        assertEquals("cancellation/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else", redirectLink);
    }

    @Test
    void buildConsentScaRedirectLinkWithInternalRequestId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getAisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildConsentScaRedirectLink("Consent123", "Authorisation123", INTERNAL_REQUEST_ID, "", ConsentType.AIS);

        assertEquals("something/Authorisation123/Consent123/{encrypted-payment-id}/Consent123/Authorisation123/{encrypted-payment-id}/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }

    @Test
    void buildPaymentScaRedirectLinkWithInternalRequestId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getPisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "");

        assertEquals("something/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }

    @Test
    void buildPaymentScaRedirectLinkWithInternalRequestIdAndInstanceId() {
        doReturn("something/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getPisRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "bank1");

        assertEquals("something/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }

    @Test
    void buildPaymentCancellationScaRedirectLinkWithInternalRequestId() {
        doReturn("cancellation/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getPisPaymentCancellationRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "");

        assertEquals("cancellation/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }

    @Test
    void buildPaymentCancellationScaRedirectLinkWithInternalRequestIdAndInstanceId() {
        doReturn("cancellation/{redirect-id}/{encrypted-consent-id}/{encrypted-payment-id}/{encrypted-consent-id}/{redirect-id}/{encrypted-payment-id}/something-else/{inr-id}").when(aspspProfileService).getPisPaymentCancellationRedirectUrlToAspsp();

        String redirectLink = redirectLinkBuilder.buildPaymentCancellationScaRedirectLink("Payment123", "Authorisation123", INTERNAL_REQUEST_ID, "bank1");

        assertEquals("cancellation/Authorisation123/{encrypted-consent-id}/Payment123/{encrypted-consent-id}/Authorisation123/Payment123/something-else/" + INTERNAL_REQUEST_ID, redirectLink);
    }

    @Test
    void buildPisConfirmationLink() {
        String confirmationLink = redirectLinkBuilder.buildPisConfirmationLink("paymentService", "paymentProduct", "paymentID", "redirectID");

        assertEquals("/v1/paymentService/paymentProduct/paymentID/authorisations/redirectID", confirmationLink);
    }

    @Test
    void buildPisCancellationConfirmationLink() {
        String confirmationLink = redirectLinkBuilder.buildPisCancellationConfirmationLink("paymentService", "paymentProduct", "paymentID", "redirectID");

        assertEquals("/v1/paymentService/paymentProduct/paymentID/cancellation-authorisations/redirectID", confirmationLink);
    }

    @Test
    void buildAisConfirmationLink() {
        String confirmationLink = redirectLinkBuilder.buildConfirmationLink("consentID", "redirectID", ConsentType.AIS);

        assertEquals("/v1/consents/consentID/authorisations/redirectID", confirmationLink);
    }

    @Test
    void buildAisConfirmationLink_exception() {
        assertThrows(UnsupportedOperationException.class,
                     () -> redirectLinkBuilder.buildConfirmationLink(StringUtils.EMPTY, StringUtils.EMPTY, ConsentType.PIIS_ASPSP));
    }

    @Test
    void buildPiisConfirmationLink() {
        String confirmationLink = redirectLinkBuilder.buildConfirmationLink("consentID", "redirectID", ConsentType.PIIS_TPP);

        assertEquals("/v2/consents/confirmation-of-funds/consentID/authorisations/redirectID", confirmationLink);
    }

    @Test
    void buildConsentScaOauthRedirectLink() {
        //Given
        doReturn("something/{redirect-id}/authorisation/{encrypted-consent-id}/Consent123/{inr-id}").when(aspspProfileService).getOauthConfigurationUrl();
        String expected = "something/redirectId/authorisation/encryptedConsentId/Consent123/5c2d5564-367f-4e03-a621-6bef76fa4208";

        //When
        String actual = redirectLinkBuilder.buildConsentScaOauthRedirectLink(ENC_CNSNT_ID, REDIRECT_ID, INTERNAL_REQUEST_ID);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void buildPaymentScaOauthRedirectLink() {
        //Given
        doReturn("something/{redirect-id}/authorisation/{encrypted-payment-id}/Consent123/{inr-id}").when(aspspProfileService).getOauthConfigurationUrl();
        String expected = "something/redirectId/authorisation/encryptedPaymentId/Consent123/5c2d5564-367f-4e03-a621-6bef76fa4208";

        //When
        String actual = redirectLinkBuilder.buildPaymentScaOauthRedirectLink(ENC_PMNT_ID, REDIRECT_ID, INTERNAL_REQUEST_ID);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void buildPaymentCancellationScaOauthRedirectLink() {
        //Given
        doReturn("something/{redirect-id}/authorisation/{encrypted-payment-id}/Consent123/{inr-id}").when(aspspProfileService).getOauthConfigurationUrl();
        String expected = "something/redirectId/authorisation/encryptedPaymentId/Consent123/5c2d5564-367f-4e03-a621-6bef76fa4208";

        //When
        String actual = redirectLinkBuilder.buildPaymentCancellationScaOauthRedirectLink(ENC_PMNT_ID, REDIRECT_ID, INTERNAL_REQUEST_ID);

        //Then
        assertEquals(expected, actual);
    }
}
