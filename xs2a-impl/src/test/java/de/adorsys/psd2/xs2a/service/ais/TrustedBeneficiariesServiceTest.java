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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aTrustedBeneficiariesMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTrustedBeneficiariesListValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetTrustedBeneficiariesListConsentObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_405;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrustedBeneficiariesServiceTest {
    private static final String CONSENT_ID = "consentId";
    private static final String ACCOUNT_ID = "accountId";
    private static final String REQUEST_URI = "requestUri";

    @InjectMocks
    private TrustedBeneficiariesService trustedBeneficiariesService;
    @Mock
    private AccountSpi accountSpi;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;
    @Mock
    private TppService tppService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetTrustedBeneficiariesListValidator getTrustedBeneficiariesListValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private AccountHelperService accountHelperService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private SpiToXs2aTrustedBeneficiariesMapper spiToXs2aTrustedBeneficiariesMapper;
    @Mock
    private SpiTrustedBeneficiaries spiTrustedBeneficiaries;
    @Mock
    private SpiAccountConsent spiAccountConsent;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    private SpiContextData spiContextData;
    private SpiAccountReference spiAccountReference;
    private AisConsent aisConsent;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
        spiAccountReference =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json",
                                         SpiAccountReference.class);
        spiContextData = TestSpiDataProvider.getSpiContextData();

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
    }

    @Test
    void getTrustedBeneficiaries_consentUnknown() {
        // Given
        ResponseObject<Xs2aTrustedBeneficiariesList> expected = getConsentUnknownResponse();
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aTrustedBeneficiariesList> actual =
            trustedBeneficiariesService.getTrustedBeneficiaries(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isTrue();
        assertThat(actual.getError()).isEqualTo(expected.getError());
    }

    @Test
    void getTrustedBeneficiaries_validationError() {
        // Given
        ResponseObject<Xs2aTrustedBeneficiariesList> expected = getValidationErrorResponse();

        GetTrustedBeneficiariesListConsentObject validatorObject = new GetTrustedBeneficiariesListConsentObject(aisConsent, ACCOUNT_ID, REQUEST_URI);
        ValidationResult invalid = ValidationResult.invalid(AIS_405, MessageErrorCode.SERVICE_INVALID_405);
        when(getTrustedBeneficiariesListValidator.validate(validatorObject)).thenReturn(invalid);

        // When
        ResponseObject<Xs2aTrustedBeneficiariesList> actual =
            trustedBeneficiariesService.getTrustedBeneficiaries(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isTrue();
        assertThat(actual.getError()).isEqualTo(expected.getError());
    }

    @Test
    void getTrustedBeneficiaries_spiError() {
        // Given
        setUpMocks();
        ResponseObject<Xs2aTrustedBeneficiariesList> expected = getSpiErrorResponse();
        SpiResponse<List<SpiTrustedBeneficiaries>> spiResponse = buildErrorSpiResponse(Collections.singletonList(spiTrustedBeneficiaries));

        when(accountSpi.requestTrustedBeneficiariesList(spiContextData, spiAccountReference, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(ErrorHolder
                                                                                           .builder(ErrorType.AIS_401)
                                                                                           .tppMessages(TppMessageInformation.of(PSU_CREDENTIALS_INVALID))
                                                                                           .build());

        // When
        ResponseObject<Xs2aTrustedBeneficiariesList> actual =
            trustedBeneficiariesService.getTrustedBeneficiaries(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isTrue();
        assertThat(actual.getError()).isEqualTo(expected.getError());
    }

    @Test
    void getTrustedBeneficiaries_success() {
        // Given
        setUpMocks();
        List<SpiTrustedBeneficiaries> beneficiaries = Collections.singletonList(spiTrustedBeneficiaries);
        SpiResponse<List<SpiTrustedBeneficiaries>> spiResponse = buildSuccessSpiResponse(beneficiaries);
        when(accountSpi.requestTrustedBeneficiariesList(spiContextData, spiAccountReference, spiAccountConsent, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        Xs2aTrustedBeneficiaries xs2aTrustedBeneficiaries =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/trusted-beneficiaries.json",
                                         Xs2aTrustedBeneficiaries.class);
        List<Xs2aTrustedBeneficiaries> trustedBeneficiaries = Collections.singletonList(xs2aTrustedBeneficiaries);
        when(spiToXs2aTrustedBeneficiariesMapper.mapToXs2aTrustedBeneficiariesList(beneficiaries)).thenReturn(trustedBeneficiaries);

        ResponseObject<Xs2aTrustedBeneficiariesList> expected =
            getSuccessResponse(new Xs2aTrustedBeneficiariesList(trustedBeneficiaries));

        // When
        ResponseObject<Xs2aTrustedBeneficiariesList> actual =
            trustedBeneficiariesService.getTrustedBeneficiaries(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isFalse();
        assertThat(actual.getBody()).isEqualTo(expected.getBody());
    }

    private void setUpMocks() {
        GetTrustedBeneficiariesListConsentObject validatorObject = new GetTrustedBeneficiariesListConsentObject(aisConsent, ACCOUNT_ID, REQUEST_URI);
        when(getTrustedBeneficiariesListValidator.validate(validatorObject)).thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData()).thenReturn(spiContextData);

        when(accountHelperService.findAccountReference(aisConsent.getAspspAccountAccesses().getAccounts(), ACCOUNT_ID)).thenReturn(spiAccountReference);
        when(consentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(spiAccountConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID)).thenReturn(spiAspspConsentDataProvider);
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getSuccessResponse(Xs2aTrustedBeneficiariesList holder) {

        return ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                   .body(holder)
                   .build();
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getSpiErrorResponse() {
        return ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                   .fail(ErrorType.AIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID))
                   .build();
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getValidationErrorResponse() {
        return ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                   .fail(AIS_405, TppMessageInformation.of(MessageErrorCode.SERVICE_INVALID_405))
                   .build();
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getConsentUnknownResponse() {
        return ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                   .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                   .build();
    }

    private SpiResponse<List<SpiTrustedBeneficiaries>> buildSuccessSpiResponse(List<SpiTrustedBeneficiaries> beneficiaries) {
        return SpiResponse.<List<SpiTrustedBeneficiaries>>builder()
                   .payload(beneficiaries)
                   .build();
    }

    private SpiResponse<List<SpiTrustedBeneficiaries>> buildErrorSpiResponse(List<SpiTrustedBeneficiaries> beneficiaries) {
        return SpiResponse.<List<SpiTrustedBeneficiaries>>builder()
                   .payload(beneficiaries)
                   .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                   .build();
    }
}
